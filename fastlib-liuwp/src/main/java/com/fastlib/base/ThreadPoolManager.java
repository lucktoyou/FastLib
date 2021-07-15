package com.fastlib.base;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.fastlib.utils.FastLog;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sgfb on 2019\03\10.
 * 线程管理
 */
public class ThreadPoolManager {
    private static final int MIN_THREAD = 5;
    public final static ThreadPoolExecutor sQuickPool;  //轻请求线程池 建议任务应小于100ms
    public final static ThreadPoolExecutor sSlowPool;   //重请求线程池 适用io、网络等延迟比较大的任务
    private static Handler sQueueHandler;
    private static Handler sMainHandler;

    static {
        int quickPoolCount = Math.max(MIN_THREAD, Runtime.getRuntime().availableProcessors() / 2);
        sQuickPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(quickPoolCount, new NamedThreadFactory("fastlib"));

        int slowPoolCount = Runtime.getRuntime().availableProcessors() + MIN_THREAD;
        sSlowPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(slowPoolCount, new NamedThreadFactory("fastlib"));

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                sQueueHandler = new Handler();
                Looper.loop();
            }
        }.start();
        sMainHandler = new Handler(Looper.getMainLooper());
        FastLog.d(String.format(Locale.getDefault(), "初始化内存池 quickPool:%d slowPool:%d", quickPoolCount, slowPoolCount));
    }

    public static Handler getQueueHandler() {
        while (sQueueHandler == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sQueueHandler;
    }

    public static Handler getMainHandler() {
        while (sMainHandler == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sMainHandler;
    }

    public static int getThreadCount() {
        return sQuickPool.getMaximumPoolSize() + sSlowPool.getMaximumPoolSize();
    }

    public static long getCompleteTaskCount() {
        return sQuickPool.getCompletedTaskCount() + sSlowPool.getCompletedTaskCount();
    }

    /**
     * 仅定义线程池名自定义工厂
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public NamedThreadFactory(String groupNamePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = groupNamePrefix + "-thread";
        }

        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
