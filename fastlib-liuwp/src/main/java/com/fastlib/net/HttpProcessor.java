package com.fastlib.net;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Pair;

import com.fastlib.net.core.ResponseCodeDefinition;
import com.fastlib.net.core.HeaderDefinition;
import com.fastlib.net.core.Method;
import com.fastlib.net.core.HttpCoreImpl;
import com.fastlib.net.download.DownloadController;
import com.fastlib.net.listener.GlobalListener;
import com.fastlib.net.listener.SimpleListener;
import com.fastlib.net.param.interpreter.ParamInterpreter;
import com.fastlib.net.param.interpreter.ParamInterpreterFactor;
import com.fastlib.net.param.interpreter.type.FormDataInterpreter;
import com.fastlib.net.upload.ValuePosition;
import com.fastlib.net.tool.Cancelable;
import com.fastlib.net.tool.StatisticalImpl;
import com.fastlib.net.tool.Statistical;
import com.fastlib.utils.core.SaveUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by sgfb on 2019/12/10
 * Modified by liuwp on 2021\11\30.
 * 此类对Http进行请求.通过{@link Request}给定的参数经过解释调用{@link HttpCoreImpl}达到请求和回调
 */
public class HttpProcessor implements Runnable, Cancelable{
    private Request mRequest;
    private InputStream mRawDataInputStream;
    private Executor mCallbackExecutor;
    private Exception mException;
    private Type mResultType;
    private Object mResultData;
    private File mDownloadFile;
    private int mStatusCode;

    public HttpProcessor(Request request){
        mRequest = request;
        mCallbackExecutor = new Executor(){
            @Override
            public void execute(Runnable command){
                if(!mRequest.getCallbackOnWorkThread())
                    new Handler(Looper.getMainLooper()).post(command);
                else command.run();
            }
        };
    }

    @Override
    public void run(){
        HttpCoreImpl httpCore = null;
        try{
            String rootAddress = HttpGlobalConfig.getInstance().getRootAddress();
            String urlWithParam = mRequest.getSkipRootAddress() ? mRequest.getUrl() : rootAddress + mRequest.getUrl();
            String method = mRequest.getMethod().toUpperCase();
            //前期准备
            if("GET".equals(method))
                urlWithParam = urlWithParam.concat(concatUrlParam());
            httpCore = new HttpCoreImpl(urlWithParam,method);
            httpCore.setCancelable(this);
            if(!mRequest.getSkipGlobalListener())
                HttpGlobalConfig.getInstance().getGlobalListener().onLaunchRequestBefore(mRequest);
            if(mRequest.getDownloadController() != null)
                mRequest.getDownloadController().prepare(mRequest);

            //判断发送体或返回体是否空
            boolean needClientBody = Method.POST.equals(method) || Method.PUT.equals(method);
            boolean needServerBody = !Method.HEAD.equals(method);

            //填充头部
            for(Map.Entry<String,List<String>> entry : mRequest.getHeader().entrySet()){
                for(String header : entry.getValue()){
                    httpCore.addHeader(entry.getKey(),header);
                }
            }

            if(needClientBody){
                //填充输出体
                String bodyType = checkBodyType();
                String contentType = genContentType(bodyType);
                if(contentType != null)
                    httpCore.addHeader(HeaderDefinition.KEY_CONTENT_TYPE,contentType);
                ParamInterpreter interpreter = ParamInterpreterFactor.getInterpreter(bodyType);
                List<ValuePosition> valuePositions = new ArrayList<>();
                List<InputStream> inputStreamList = interpreter.interpreter(mRequest.getRequestParam(),valuePositions);
                for(InputStream inputStream : inputStreamList)
                    httpCore.addPendingInputStream(inputStream);
                httpCore.buildUploadMonitorProxy(mRequest.getUploadingMonitor(),valuePositions);
            }

            //开始连接
            if(mRequest.getConnectionTimeout() > 0)
                httpCore.setConnectionTimeout(mRequest.getConnectionTimeout());
            if(mRequest.getReadTimeout() > 0)
                httpCore.setReadTimeout(mRequest.getReadTimeout());
            if(mRequest.isCanceled())
                throw new CustomException("手动取消");
            httpCore.begin();

            mRequest.setRequestHeader(httpCore.getRequestHeader());
            mRequest.setResponseHeader(httpCore.getResponseHeader());
            if(needServerBody){
                InputStream in = httpCore.getInputStream();
                mResultType = mRequest.getResultType();
                DownloadController downloadController = mRequest.getDownloadController();

                if(downloadController == null && mResultType == File.class)
                    throw new IllegalStateException("未设置下载控制器");

                //下载类型
                if(downloadController != null){
                    long fileLength = -1;
                    String filename = null;

                    String fileLengthHeader = httpCore.getResponseHeader().getHeaderFirst(HeaderDefinition.KEY_CONTENT_LENGTH);
                    if(fileLengthHeader != null)
                        fileLength = Long.parseLong(fileLengthHeader);
                    String contentDisposition = httpCore.getResponseHeader().getHeaderFirst(HeaderDefinition.KEY_CONTENT_DISPOSITION);
                    if(contentDisposition != null){
                        int filenameIndex = contentDisposition.indexOf("filename=\"");
                        if(filenameIndex != -1)
                            filename = new String(contentDisposition.substring(filenameIndex + 10,contentDisposition.length() - 1).getBytes("ISO_8859_1"),"UTF-8");
                    }
                    downloadController.onStreamReady(in,filename,fileLength);
                    mDownloadFile = downloadController.getTargetFile();
                    mRawDataInputStream = new FileInputStream(mDownloadFile);
                }else
                    mRawDataInputStream = new ByteArrayInputStream(SaveUtil.loadInputStream(in,false));
            }else mRawDataInputStream = new ByteArrayInputStream(new byte[]{});
            httpCore.end();

            mStatusCode = httpCore.getResponseHeader().getCode();
            int sendLength = httpCore.getSendHeaderLength() + httpCore.getSendBodyLength();
            int receivedLength = httpCore.getReceivedHeaderLength() + httpCore.getReceivedBodyLength();
            mRequest.setStatistical(new StatisticalImpl(httpCore.getHttpTimer(),new Statistical.ContentLength(sendLength,receivedLength)));
        }catch(Exception e){
            mException = e;
            if(httpCore != null)
                httpCore.discard();
        }finally{
            mCallbackExecutor.execute(new Runnable(){
                @Override
                public void run(){
                    callbackProcess();
                }
            });
        }
    }

    /**
     * 转换参数为url参数
     *
     * @return url地址参数
     */
    private String concatUrlParam() throws IOException{
        List<InputStream> streamList = ParamInterpreterFactor.getInterpreter(ParamInterpreterFactor.BODY_URL_PARAM).interpreter(mRequest.getRequestParam(),new ArrayList<ValuePosition>());
        return streamList != null && !streamList.isEmpty() ? new String(SaveUtil.loadInputStream(streamList.get(0),true)) : "";
    }

    /**
     * 自动检查body类型返回.
     * @return body类型
     */
    private @ParamInterpreterFactor.ParamInterpreterType
    String checkBodyType(){
//        //先检查自定义头部是否有Content-Type如果可以识别则使用不可识别或不存在，再
//        for(Map.Entry<String,List<String>> entry : mRequest.getHeader().entrySet()){
//            for(String value : entry.getValue()){
//                String headKey = entry.getKey();
//                if(headKey.equals(HeaderDefinition.KEY_CONTENT_TYPE)){
//                    switch(value){
//                        case ParamInterpreterFactor.BODY_FORM_DATA:
//                            return ParamInterpreterFactor.BODY_FORM_DATA;
//                        case ParamInterpreterFactor.BODY_RAW_JSON:
//                            return ParamInterpreterFactor.BODY_RAW_JSON;
//                        case ParamInterpreterFactor.BODY_FORM_URLENCODED:
//                            return ParamInterpreterFactor.BODY_FORM_URLENCODED;
//                    }
//                }
//            }
//        }
        //根据参数检查form-data、json、默认form-urlencoded
        List<Pair<String,Object>> bottomParam = mRequest.getRequestParam().getBottomParam();
        for(Pair<String,Object> pair : bottomParam){
            if(pair.second instanceof File)
                return ParamInterpreterFactor.BODY_FORM_DATA;
        }
        if(!bottomParam.isEmpty())
            return ParamInterpreterFactor.BODY_RAW_JSON;
        return ParamInterpreterFactor.BODY_FORM_URLENCODED;
    }

    /**
     * 根据body类型返回ContentType
     *
     * @return Content-Type头部值 如果是null则为空
     */
    private String genContentType(String bodyType){
        switch(bodyType){
            case ParamInterpreterFactor.BODY_FORM_DATA:
                return HeaderDefinition.VALUE_CONTENT_TYPE_MULTIPART_FORM_DATA + "; boundary=" + FormDataInterpreter.BOUNDARY;
            case ParamInterpreterFactor.BODY_RAW_JSON:
                return HeaderDefinition.VALUE_CONTENT_TYPE_JSON;
            case ParamInterpreterFactor.BODY_FORM_URLENCODED:
                return HeaderDefinition.VALUE_CONTENT_TYPE_X_WWW_FORM_URLENCODED;
            default:
                return null;
        }
    }

    /**
     * 回调回请求方
     */
    @SuppressWarnings("unchecked")
    private void callbackProcess(){
        final GlobalListener globalListener = mRequest.getSkipGlobalListener() ?
                HttpGlobalConfig.getInstance().getEmptyGlobalListener() : HttpGlobalConfig.getInstance().getGlobalListener();
        final SimpleListener listener = mRequest.getListener() == null ?
                HttpGlobalConfig.getInstance().getEmptyListener() : mRequest.getListener();

        //此包裹监听将回调先经过globalListener然后走原监听
        SimpleListener wrapperListener = new SimpleListener(){
            @Override
            public byte[] onRawData(Request request,byte[] data){
                byte[] bytes = globalListener.onRawDataListener(request,data);
                return listener.onRawData(request,bytes);
            }

            @Override
            public void onResponseSuccess(Request request,Object result){
                Object handledResult = globalListener.onResponseSuccessListener(request,result);
                listener.onResponseSuccess(request,handledResult);
                mResultData = handledResult;
            }

            @Override
            public void onError(Request request,Exception error){
                Exception handledException = globalListener.onErrorListener(request,error);
                listener.onError(request,handledException);
            }
        };

        if(mException == null){
            try{
                byte[] bytes = SaveUtil.loadInputStream(mRawDataInputStream,false);
                if(mStatusCode == ResponseCodeDefinition.OK){
                    bytes = wrapperListener.onRawData(mRequest,bytes);
                    if(mResultType == void.class || mResultType == Void.class)
                        wrapperListener.onResponseSuccess(mRequest,null);
                    else if(mResultType == null || mResultType == Object.class || mResultType == byte[].class)
                        wrapperListener.onResponseSuccess(mRequest,bytes);
                    else if(mResultType == File.class)
                        wrapperListener.onResponseSuccess(mRequest,mDownloadFile);
                    else if(mResultType == String.class)
                        wrapperListener.onResponseSuccess(mRequest,new String(bytes));
                    else{
                        Gson gson = new Gson();
                        String json = new String(bytes);
                        try{
                            Object obj = gson.fromJson(json,mResultType);
                            wrapperListener.onResponseSuccess(mRequest,obj);
                        }catch(JsonSyntaxException e){
                            wrapperListener.onError(mRequest,e);
                        }
                    }
                }else{
                    Exception ex = new CustomException("网络响应异常(" + mStatusCode + ").");//响应内容: new String(bytes)
                    wrapperListener.onError(mRequest,ex);
                }
            }catch(IOException e){
                //这里仅关闭流时可能出现的异常，不处理
            }finally{
                try{
                    mRawDataInputStream.close();
                }catch(IOException e){
                    //这里仅关闭流时可能出现的异常，不处理
                }
            }
        }else wrapperListener.onError(mRequest,mException);
    }

    public Object getResultData(){
        return mResultData;
    }

    @Override
    public boolean isCanceled(){
        return mRequest != null && mRequest.isCanceled();
    }
}
