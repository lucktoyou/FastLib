package com.example.fastlibdemo.task;

import com.example.fastlibdemo.R;
import com.example.fastlibdemo.base.BindViewActivity;
import com.example.fastlibdemo.databinding.ActivityTaskBinding;
import com.fastlib.annotation.Bind;
import com.fastlib.base.task.Action;
import com.fastlib.base.task.EmptyAction;
import com.fastlib.base.task.NoParamAction;
import com.fastlib.base.task.NoReturnAction;
import com.fastlib.base.task.Task;
import com.fastlib.base.task.ThreadType;
import com.fastlib.net.Request;
import com.fastlib.net.listener.SimpleListener;
import com.fastlib.utils.FastLog;
import com.fastlib.utils.N;

import java.util.List;

public class TaskActivity extends BindViewActivity<ActivityTaskBinding>{
    private static String TAG = TaskActivity.class.getSimpleName() + "===>";


    @Override
    public void alreadyPrepared(){

    }

    @Bind(R.id.btnTask)
    public void startTask(){
        publishMoments("小红","小花","小华");
    }


    /**
     * 发布朋友圈文章
     *
     * @param images 多图像本地路径
     */
    private void publishMoments(String... images){
        startTask(Task.beginCycle(images) //使用beginCycle展开数组
                        .next(new Action<String,Request>(){

                            @Override
                            protected Request execute(String param){
                                return new Request("http://tjgl.gongriver.com/api/v1/NewsClass/news/classNamesLists")
                                        .setSkipRootAddress(true)
                                        .setSkipGlobalListener(true)
                                        .put("page",1)
                                        .put("size",10)
                                        .put("className","病虫害识别");
                            }
                        })
                        .next(new Action<Request,String>(){
                            @Override
                            protected String execute(Request param){
                                param.setListener(new SimpleListener<String>(){
                                    @Override
                                    public void onResponseSuccess(Request request,String result){
                                        if(result == null){
                                            stopTask();
                                        }
                                    }

                                    @Override
                                    public void onError(Request request,Exception error){
                                        FastLog.e(error.toString());
                                    }
                                });
                                return (String)param.startSync(String.class);
                            }
                        })
                        .again(new NoReturnAction<List<String>>(){ //使用again来结束之前的循环并且获取结果数组

                            @Override
                            public void executeAdapt(List<String> param){
                                StringBuilder sb = new StringBuilder();
                                for(String image : param)
                                    sb.append(image).append("|");
                                if(sb.length() > 0)
                                    sb.deleteCharAt(sb.length() - 1);
                                FastLog.d("again " + sb.toString());
                            }

                        },ThreadType.MAIN)//指定回调运行在主线程中
                ,new NoReturnAction<Throwable>(){
                    @Override
                    public void executeAdapt(Throwable param){
                        FastLog.d("exception handle " + Thread.currentThread().getName());
                    }
                }
                ,new EmptyAction(){
                    @Override
                    protected void executeAdapt(){
                        FastLog.d("last action " + Thread.currentThread().getName());
                    }
                }
        );
    }

    @Bind(R.id.btnTask2)
    public void startTask2(){
        publishMoments2("小红","小花","小华");
    }

    private void publishMoments2(final String... images){
        startTask(Task.begin(images)
                .cycle(new Action<String[],String[]>(){
                    @Override
                    protected String[] execute(String[] params){
                        FastLog.d("cycle " + Thread.currentThread().getName());
                        return params;
                    }
                })
                .next(new Action<String,String>(){
                    @Override
                    protected String execute(String param) throws Throwable{
                        FastLog.d("next " + Thread.currentThread().getName());
                        return "<" + param;
                    }
                })
                .next(new Action<String,String>(){
                    @Override
                    protected String execute(String param) throws Throwable{
                        FastLog.d("next " + Thread.currentThread().getName());
                        return param + ">";
                    }
                })
                .filter(new Action<String,Boolean>(){
                    @Override
                    protected Boolean execute(String param){
                        FastLog.d("filter " + Thread.currentThread().getName());
                        if("<小花>".equals(param)){
                            return true;
                        }else {
                            return false;
                        }
                    }
                })
                .again(new NoReturnAction<List<String>>(){
                    @Override
                    public void executeAdapt(List<String> param){
                        FastLog.d("again " + Thread.currentThread().getName() + " " + param.toString());
                    }
                },ThreadType.MAIN)
        );
    }


    @Bind(R.id.btnTask3)
    public void startTask3(){
        startTask(Task.begin()
                .next(new NoParamAction<Integer>(){

                    @Override
                    protected Integer executeAdapt(){
                        return 100;
                    }
                })
                .next(new Action<Integer,String>(){
                    @Override
                    protected String execute(Integer param){
                        FastLog.d("next " + Thread.currentThread().getName());
                        return "<" + param;
                    }
                })
                .next(new Action<String,String>(){
                    @Override
                    protected String execute(String param){
                        FastLog.d("next " + Thread.currentThread().getName());
                        return param + ">";
                    }
                })
                .filter(new Action<String,Boolean>(){
                    @Override
                    protected Boolean execute(String param){
                        FastLog.d("filter " + Thread.currentThread().getName());
                        return true;
                    }
                })
                .again(new NoReturnAction<List<String>>(){
                    @Override
                    public void executeAdapt(List<String> param){
                        FastLog.d("again " + Thread.currentThread().getName() + " " + param.toString());
                    }
                },ThreadType.MAIN)
        );
    }
}

