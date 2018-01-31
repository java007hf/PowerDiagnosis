package com.tencent.powerhook.hookfun;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class WifiManagerHook implements TosPlugInterface {
	
	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		switch(prefix) {
		case 1:
			if (method_name.equals("startScan")) {
				before_startScan(param);
			}
			break;
		default:
			break;
		}
	}

	public void before_startScan(MethodPlugParam param) {
		LogHelper.d("========before_startScan=========");
		ProcessStatsHelper.getInstance().noteStartWifiScan(new Throwable());
	}

}
