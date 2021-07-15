package com.fastlib.bean.event;

/**
 * Created by liuwp on 2021/6/9.
 *  倒计时进度广播事件。
 */
public class EventCountDown {

    private int eventId;
    private long millisInFuture;
    private long countDownInterval;
    private long millisUntilFinished;

    public EventCountDown(int eventId,long millisInFuture, long countDownInterval, long millisUntilFinished) {
        this.eventId = eventId;
        this.millisInFuture = millisInFuture;
        this.countDownInterval = countDownInterval;
        this.millisUntilFinished = millisUntilFinished;
    }

    public int getEventId() {
        return eventId;
    }

    public long getMillisInFuture() {
        return millisInFuture;
    }

    public long getCountDownInterval() {
        return countDownInterval;
    }

    public long getMillisUntilFinished() {
        return millisUntilFinished;
    }
}


