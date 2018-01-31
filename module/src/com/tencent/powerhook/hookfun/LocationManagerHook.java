package com.tencent.powerhook.hookfun;

import android.app.PendingIntent;
import android.location.LocationListener;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class LocationManagerHook implements TosPlugInterface {
	
	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		LogHelper.d("========invoke=========");
		
		switch (prefix) {
		case 1:
			if (method_name.equals("requestLocationUpdates")) {
				before_requestLocationUpdates(param);
			} else if (method_name.equals("removeUpdates")) {
				before_removeUpdates(param);
			}
			break;
		default:
			break;
		}
	}

	public void before_requestLocationUpdates(MethodPlugParam param) {
		LocationListener listener = (LocationListener) param.args[1];
		PendingIntent intent = (PendingIntent)param.args[3];
		
		LogHelper.d("========before_requestLocationUpdates========= listener "
		+ listener + " intent " + intent);
		
		if (listener != null) {
			ProcessStatsHelper.getInstance().noteRequestLocationUpdates(listener, new Throwable());
		} else if (intent != null) {
			ProcessStatsHelper.getInstance().noteRequestLocationUpdates(intent, new Throwable());
		}
	}

	public void before_removeUpdates(MethodPlugParam param) {
		LogHelper.d("========before_removeUpdates=========");
		Object o = param.args[0];
		
		if (o != null) {
			if (o instanceof LocationListener) {
				LocationListener listener = (LocationListener) o;
				ProcessStatsHelper.getInstance().noteRemoveUpdates(listener);
			} else if (o instanceof PendingIntent) {
				PendingIntent intent = (PendingIntent) o;
				ProcessStatsHelper.getInstance().noteRemoveUpdates(intent);
			}
		}
	}
}
