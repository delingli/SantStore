package com.hai.store.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * create by ldl2018/8/22 0022
 */

public abstract class BaseFragment extends Fragment {
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final int SIZE = CORES;
    private static final int SIZE_MAX = CORES * 2;
    private static final BlockingQueue<Runnable> QUEUE = new LinkedBlockingQueue<>(16);

    private static final ThreadFactory FACTORY = new ThreadFactory() {
        private final AtomicInteger COUNT = new AtomicInteger(1);

        @Override
        public Thread newThread(@SuppressWarnings("NullableProblems") Runnable runnable) {
            return new Thread(runnable, "PRESENT #" + COUNT.getAndIncrement());
        }
    };
    protected static final ExecutorService EXECUTOR = new ThreadPoolExecutor(SIZE, SIZE_MAX, 60,
            SECONDS, QUEUE, FACTORY);

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findView(view);
    }

    public abstract void findView(View view); //1.find_view

    public abstract void setLogic(); //2.加载逻辑

    public abstract void loadData(); //3.加载数据

    public abstract void showView(); //4.展示数据

    public abstract void showLoading(); //显示正在加载

    public abstract void showError(); //显示加载错误
}
