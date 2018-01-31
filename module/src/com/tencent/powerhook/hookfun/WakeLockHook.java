package com.tencent.powerhook.hookfun;

import android.os.PowerManager.WakeLock;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.ReflectOptUtils;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class WakeLockHook implements TosPlugInterface {

	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		switch(prefix) {
		case 1:
			if (method_name.equals("acquireLocked")) {
				before_acquireLocked(param);
			} else if (method_name.equals("release")) {
				before_release(param);
			}
			break;
		default:
			break;
		}
	}
	
	public void before_acquireLocked(MethodPlugParam param) {
		LogHelper.d("========before_acquireLocked=========");
		WakeLock wakeLock = (WakeLock) param.thisObject;
		boolean mRefCounted = (Boolean) ReflectOptUtils.getFieldValue("mRefCounted", wakeLock, WakeLock.class);
		int mCount = (Integer) ReflectOptUtils.getFieldValue("mCount", wakeLock, WakeLock.class);
		
		if (!mRefCounted || mCount++ == 0) {
			String mTag = (String) ReflectOptUtils.getFieldValue("mTag", wakeLock, WakeLock.class);
			ProcessStatsHelper.getInstance().noteStartWakelock(mTag, new Throwable());
		}
	}
	
	public void before_release(MethodPlugParam param) {
		LogHelper.d("========before_release=========");
		WakeLock wakeLock = (WakeLock) param.thisObject;
		boolean mRefCounted = (Boolean) ReflectOptUtils.getFieldValue("mRefCounted", wakeLock, WakeLock.class);
		int mCount = (Integer) ReflectOptUtils.getFieldValue("mCount", wakeLock, WakeLock.class);
		boolean mHeld = (Boolean) ReflectOptUtils.getFieldValue("mHeld", wakeLock, WakeLock.class);
		
		if (!mRefCounted || --mCount == 0) {
            if (mHeld) {
            	String mTag = (String) ReflectOptUtils.getFieldValue("mTag", wakeLock, WakeLock.class);
            	ProcessStatsHelper.getInstance().noteStopWakelock(mTag);
            }
        }
	}
}
