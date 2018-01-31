package com.tencent.qrom.powerdiagnosis.process;

import java.util.HashSet;
import java.util.Set;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Bundle;

public class ProcessTraceItem {
	private RunningAppProcessInfo info;
	private boolean checked;
	private boolean enabled = true;
	private long startRecordTime = 0;
	private long stopRecordTime = 0;
	private String path = "";
	
	private Object mLock = new Object();
	private Set<StatusChangedObserver> mChangedObserver = new HashSet<StatusChangedObserver>();

	interface StatusChangedObserver {
		void onEnabledChanged(ProcessTraceItem p, boolean b);
		void onCheckedChange(ProcessTraceItem p, boolean b);
		void onPathChange(ProcessTraceItem p, String path);
		void onExtrChange(ProcessTraceItem p, Bundle bundle);
	}
	
	public void attach(StatusChangedObserver observer){
		synchronized (mLock) {
			if(observer != null && !mChangedObserver.contains(observer)) {
				mChangedObserver.add(observer);
			}
		}
    }

	public void detach(StatusChangedObserver observer) {
		synchronized (mLock) {
			if(observer != null && mChangedObserver.contains(observer)) {
				mChangedObserver.remove(observer);
			}
		}
	}
	
	public void onExtrChange(Bundle bundle) {
		synchronized (mLock) {
			for(StatusChangedObserver observer : mChangedObserver) {
				observer.onExtrChange(this, bundle);
			}
		}
	}
	
	public void setStartRecordTime(long startRecordTime) {
		this.startRecordTime = startRecordTime;
	}
	
	public void setStopRecordTime(long stopRecordTime) {
		this.stopRecordTime = stopRecordTime;
	}
	
	public long getStartRecordTime() {
		return startRecordTime;
	}
	
	public long getStopRecordTime() {
		return stopRecordTime;
	}
	
	public void setPath(String path) {
		this.path = path;
		
		synchronized (mLock) {
			for(StatusChangedObserver observer : mChangedObserver) {
				observer.onPathChange(this, path);
			}
		}
	}
	
	public String getPath() {
		return path;
	}
	
	public RunningAppProcessInfo getInfo() {
		return info;
	}
	
	public void setInfo(RunningAppProcessInfo info) {
		this.info = info;
	}

	public boolean isChecked() {
		return checked;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		synchronized (mLock) {
			for(StatusChangedObserver observer : mChangedObserver) {
				observer.onEnabledChanged(this, enabled);
			}
		}
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
		synchronized (mLock) {
			for(StatusChangedObserver observer : mChangedObserver) {
				observer.onCheckedChange(this, checked);
			}
		}
	}

	@Override
	public String toString() {
		return "Process(" + info.uid + ", " + info.pid + ", " + info.processName + "):" +
				"checked(" + checked + ")："
				+"enabled("+enabled+")";
	}
}
