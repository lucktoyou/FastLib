package com.fastlib.base.custom;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.fastlib.base.EventObserver;
import com.fastlib.bean.event.EventCountDown;
import com.fastlib.utils.FastLog;

/**
 * Created by liuwp on 2021/6/9.
 * 倒计时服务。
 */
public class CountDownService extends Service {

    public static final String MILLIS_IN_FUTURE = "millisInFuture";
    public static final String COUNT_DOWN_INTERVAL = "countDownInterval";
    public static final String EVENT_ID = "eventId";//倒计时进度广播事件id.

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final int eventId = intent.getIntExtra(EVENT_ID,0);
            final long millisInFuture = intent.getLongExtra(MILLIS_IN_FUTURE, 0);
            final long countDownInterval = intent.getLongExtra(COUNT_DOWN_INTERVAL, 0);
            CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

                @Override
                public void onTick(long millisUntilFinished) {
                    EventObserver.getInstance().sendEvent(
                            new EventCountDown(eventId,millisInFuture, countDownInterval, millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    EventObserver.getInstance().sendEvent(
                            new EventCountDown(eventId,millisInFuture, countDownInterval, 0));
                    stopSelf();
                }
            };
            countDownTimer.start();
        }else {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
