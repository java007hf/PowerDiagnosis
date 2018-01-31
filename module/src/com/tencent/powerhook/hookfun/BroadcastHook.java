package com.tencent.powerhook.hookfun;

import android.content.Intent;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class BroadcastHook implements TosPlugInterface {

	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		
	}

	public void before_broadcastIntent(MethodPlugParam param) {
		LogHelper.d("========before_broadcastIntent=========");
		Intent intent = (Intent)param.args[1];
		String action = intent.getAction();
		
		LogHelper.d("========before_broadcastIntent====action=====" + action);
		
		if (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE.equals(action)) {
			ProcessStatsHelper.getInstance().noteStartMediaScan(new Throwable());
		}
	}
}
