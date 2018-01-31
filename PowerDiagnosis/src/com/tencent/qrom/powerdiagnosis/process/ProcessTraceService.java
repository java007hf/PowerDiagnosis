package com.tencent.qrom.powerdiagnosis.process;

import com.tencent.qrom.powerdiagnosis.process.ProcessTraceManager.ProcessTraceObserver;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class ProcessTraceService extends Service implements ProcessTraceObserver {
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	LogHelper.v("ProcessTraceService running");
    	ProcessTraceManager.getInstance(getApplicationContext()).attach(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	LogHelper.v("ProcessTraceService stop");
    	ProcessTraceManager.getInstance(getApplicationContext()).detach(this);
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onTraceStartResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("===ProcessTraceActivity.onTraceStartResult" + item);
		item.setEnabled(true);
		item.setChecked(true);
		long startTime = more.getLong("STARTTIME");
		item.setStartRecordTime(startTime);
	}

	@Override
	public void onTraceStopResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("===ProcessTraceActivity.onTraceStopResult" + item);
		item.setEnabled(true);
		item.setChecked(false);
		long stopTime = more.getLong("STOPTIME");
		item.setStopRecordTime(stopTime);
		
		String path = more.getString("PATH");
		item.setPath(path);
	}

	@Override
	public void onTraceClearResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("===ProcessTraceActivity.onTraceClearResult" + item);
		item.setEnabled(true);
		long startTime = more.getLong("STARTTIME");
		item.setStartRecordTime(startTime);
	}

	@Override
	public void onTraceGeneralResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("===ProcessTraceDetailActivity.onTraceGeneralResult" + item);
		item.onExtrChange(more);
	}
}
