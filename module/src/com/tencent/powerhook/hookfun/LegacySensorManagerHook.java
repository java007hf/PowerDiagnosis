package com.tencent.powerhook.hookfun;

import android.hardware.SensorListener;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class LegacySensorManagerHook implements TosPlugInterface {
	
	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		switch (prefix) {
		case 1:
			if (method_name.equals("registerLegacyListener")) {
				before_registerLegacyListener(param);
			} else if (method_name.equals("unregisterLegacyListener")) {
				before_unregisterLegacyListener(param);
			}
			break;
		default:
			break;
		}
	}

	public void before_registerLegacyListener(MethodPlugParam param) {
		LogHelper.d("========before_registerLegacyListener====1=====");
		int sensors = (Integer) param.args[3];
		int legacyType = (Integer) param.args[0];
		SensorListener listener = (SensorListener) param.args[2];
		if ((sensors & legacyType) != 0) {
			ProcessStatsHelper.getInstance().noteRegisterLegacyListener(listener, legacyType, new Throwable());
		}
	}
	
	public void before_unregisterLegacyListener(MethodPlugParam param) {
		LogHelper.d("========before_unregisterLegacyListener====2=====");
		int sensors = (Integer) param.args[3];
		int legacyType = (Integer) param.args[0];
		SensorListener listener = (SensorListener) param.args[2];
		if ((sensors & legacyType) != 0) {
			ProcessStatsHelper.getInstance().noteUnregisterLegacyListener(listener, legacyType);
		}
	}
}
