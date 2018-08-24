package com.hai.store.keepalive.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hai.store.utils.Device;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

abstract class ApiAbs {

    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final int SIZE = CORES;
    private static final int SIZE_MAX = CORES * 2;

    private static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<>(16);

    private static final ThreadFactory FACTORY = new ThreadFactory() {
        private final AtomicInteger COUNT = new AtomicInteger(1);

        @Override
        public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
            return new Thread(runnable, "THREAD_API #" + COUNT.getAndIncrement());
        }
    };
    static final ExecutorService EXECUTOR = new ThreadPoolExecutor(SIZE, SIZE_MAX, 1,
            SECONDS, QUEUE, FACTORY);

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    final Map<String, String> COMPANY;

    ApiAbs(Context context) {
        COMPANY = Device.getDeviceInfo(context);
    }

    synchronized <T> void callback(Callback<T> c, T t) {
        callback(c, t, null);
    }

    private synchronized <T> void callback(Callback<T> c, T t, Object tag) {
        callback(c, true, t, -1, tag);
    }

    synchronized <T> void callback(Callback<T> c, int code) {
        callback(c, code, null);
    }

    private synchronized <T> void callback(Callback<T> c, int code, Object tag) {
        callback(c, false, null, code, tag);
    }

    private synchronized <T> void callback(final Callback<T> c, final boolean isSuccess, final T t,
                                           final int code, final Object tag) {
        if (null == c) return;
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                c.onFinish(isSuccess, t, code, tag);
            }
        });
    }
}