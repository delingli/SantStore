package com.hai.store.keepalive.jss;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import static android.app.job.JobInfo.NETWORK_TYPE_UNMETERED;


public class PulseService extends Service {

    private static final String TAG = "PulseService";

    /**
     * 每5分钟检查一次链接状态，确保service 存活
     */
    public static final int KEEP_ALIVE_INTERVAL = 5 * 60 * 1000;
    public static final int TEST_KEEP_ALIVE_INTERVAL = 30 * 1000;

    /**
     * ACTION Start
     */
    private static final String ACTION_START = "PulseService.Action.Start";
    /**
     * ACTION Alarm
     */
    private static final String ACTION_ALARM = "PulseService.Action.Alarm";
    /**
     * ACTION end start
     */
    private static final String ACTION_END_START = "PulseService.Action.EndStart";

    private boolean mIsAddAliveAlarm = false;

    public PulseService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (!mIsAddAliveAlarm) {
            addAliveAlarm();
            mIsAddAliveAlarm = true;
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent pIntent, int pFlags, int pStartId) {
        if (null != pIntent) {
            switch (pIntent.getAction()) {
                case ACTION_START:

                    break;
                case ACTION_ALARM:

                    break;
                case ACTION_END_START:

                    break;
            }
        }
        if (!mIsAddAliveAlarm) {
            addAliveAlarm();
            mIsAddAliveAlarm = true;
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent pIntent) {
        onEnd();
    }

    @Override
    public void onDestroy() {
        onEnd();
        super.onDestroy();
    }

    private void onEnd() {
        startService(getIntentEndStart(getApplicationContext()));
    }

    /**
     * 添加重复唤醒闹钟，用于不停唤起服务
     */
    private void addAliveAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder _Builder = new JobInfo.Builder(0, new ComponentName(getApplication(), JobSchedulerService.class));
            _Builder.setPeriodic(KEEP_ALIVE_INTERVAL);
            _Builder.setPersisted(true);
            JobScheduler _JobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            _JobScheduler.schedule(_Builder.build());
            _Builder.setRequiredNetworkType(NETWORK_TYPE_UNMETERED);
        } else {
            PendingIntent _PendingIntent = PendingIntent.getService(this, 0, getIntentAlarm(this), PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager _AlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            _AlarmManager.cancel(_PendingIntent);
            _AlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, _PendingIntent);
        }
    }

    public static Intent getIntentStart(Context pContext) {
        return getActionIntent(pContext, ACTION_START);
    }

    public static Intent getIntentAlarm(Context pContext) {
        return getActionIntent(pContext, ACTION_ALARM);
    }

    public static Intent getIntentEndStart(Context pContext) {
        return getActionIntent(pContext, ACTION_END_START);
    }

    private static Intent getActionIntent(Context pContext, String pAction) {
        Intent _Intent = new Intent(pContext, PulseService.class);
        _Intent.setAction(pAction);
        return _Intent;
    }
}
