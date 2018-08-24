package com.hai.store.keepalive.jss;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        startPulseService();
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        startPulseService();
        return false;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        startPulseService();
    }

    public void startPulseService() {
        startService(PulseService.getIntentAlarm(this));
    }
}
