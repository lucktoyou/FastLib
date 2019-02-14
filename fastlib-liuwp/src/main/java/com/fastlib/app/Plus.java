package com.fastlib.app;

import android.content.Context;

import com.fastlib.bean.NetFlow;
import com.fastlib.db.And;
import com.fastlib.db.Condition;
import com.fastlib.db.FastDatabase;
import com.fastlib.db.FunctionCommand;
import com.fastlib.net.NetManager;

/**
 * 增强功能
 */
public class Plus{

    /**
     * 保存一下流量使用情况，如果未使用不保存
     */
    public static void saveNetFlow(Context context){
        NetFlow netFlow =new NetFlow();
        netFlow.requestCount= NetManager.getInstance().mRequestCount;
        netFlow.receiveByte= NetManager.getInstance().Rx;
        netFlow.takeByte= NetManager.getInstance().Tx;
        netFlow.time= System.currentTimeMillis();

        NetFlow existsHistory= FastDatabase.getDefaultInstance(context).setFilter(And.condition(Condition.equal(netFlow.time))).getFirst(NetFlow.class);
        if(existsHistory!=null){
            netFlow.requestCount+=existsHistory.requestCount;
            netFlow.receiveByte+=existsHistory.receiveByte;
            netFlow.takeByte+=existsHistory.takeByte;
        }
        if(netFlow.requestCount>0)
            FastDatabase.getDefaultInstance(context).saveOrUpdate(netFlow);
    }

    /**
     * 获取流量使用情况
     * @param context 上下文
     * @return 流量使用情况总和
     */
    public static NetFlow getUsedNet(Context context){
        return FastDatabase.getDefaultInstance(context)
                .putFunctionCommand("requestCount", FunctionCommand.sum())
                .putFunctionCommand("receiveByte", FunctionCommand.sum())
                .putFunctionCommand("takeByte", FunctionCommand.sum())
                .getFirst(NetFlow.class);
    }
}