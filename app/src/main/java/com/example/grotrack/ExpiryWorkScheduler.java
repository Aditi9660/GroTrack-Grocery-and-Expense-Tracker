package com.example.grotrack;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Data;
import java.util.concurrent.TimeUnit;

public final class ExpiryWorkScheduler {
    private static final String PERIODIC_NAME="grotrack-periodic-expiry-check";
    private static final String TEST_NAME="grotrack-expiry-test-check";
    private ExpiryWorkScheduler() { }

    public static void schedulePeriodic(Context context) {
        PeriodicWorkRequest request=new PeriodicWorkRequest.Builder(ExpiryCheckWorker.class,24,TimeUnit.HOURS).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniquePeriodicWork(PERIODIC_NAME,ExistingPeriodicWorkPolicy.KEEP,request);
    }

    public static void runTestNow(Context context) {
        Data input=new Data.Builder().putBoolean(ExpiryCheckWorker.KEY_TEST_RUN,true).build();
        OneTimeWorkRequest request=new OneTimeWorkRequest.Builder(ExpiryCheckWorker.class).setInputData(input).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(TEST_NAME,ExistingWorkPolicy.REPLACE,request);
    }
}
