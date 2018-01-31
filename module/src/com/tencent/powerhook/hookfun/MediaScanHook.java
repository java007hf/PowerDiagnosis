package com.tencent.powerhook.hookfun;

import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class MediaScanHook implements TosPlugInterface {

	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		
	}

	public void before_scanFile(MethodPlugParam param) {
		LogHelper.d("========before_scanFile=========");
		ProcessStatsHelper.getInstance().noteStartMediaScan(new Throwable());
	}
}
