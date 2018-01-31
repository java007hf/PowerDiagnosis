package com.tencent.powerhook.hookfun;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class SystemSensorManagerHook implements TosPlugInterface {
	public static final int TOTAL_SENSOR_TYPE = 0x100010;
	
	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
	}
	
	public void before_registerListenerImpl(MethodPlugParam param) {
		LogHelper.d("=====before_registerListenerImpl====3");
		SensorEventListener listener = (SensorEventListener) param.args[0];
		Sensor sensor = (Sensor) param.args[1];
		int delayUs = (Integer) param.args[2];
		int maxBatchReportLatencyUs = (Integer) param.args[4];
		
		if (listener == null || sensor == null) {
            return;
        }
		
        if (maxBatchReportLatencyUs < 0 || delayUs < 0) {
            return;
        }
        
        ProcessStatsHelper.getInstance().noteRegisterLegacyListener(listener, sensor.getType(), new Throwable());
	}
	
	public void before_unregisterListenerImpl(MethodPlugParam param) {
		LogHelper.d("powerdiagnosis", "========before_unregisterListenerImpl=======45==");
		SensorEventListener listener = (SensorEventListener) param.args[0];
		Sensor sensor = (Sensor) param.args[1];
		
		int type = 0;
		if (sensor == null) {
			type = TOTAL_SENSOR_TYPE;
		} else {
			type = sensor.getType();
		}
		ProcessStatsHelper.getInstance().noteUnregisterLegacyListener(listener, type);
	}

}
