package com.tencent.powerhook.hookfun;

import com.tencent.powerhook.TosPlugInterface;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class AlarmManagerHook implements TosPlugInterface {
	
	@Override
	public void invoke(String method_name, int prefix, MethodPlugParam param) {
		switch(prefix) {
		case 1:
			if (method_name.equals("setImpl")) {
				before_setImpl(param);
			} else if (method_name.equals("cancel")) {
				before_cancel(param);
			}
			break;
		default:
			break;
		}
	}

	public void before_setImpl(MethodPlugParam param) {
		LogHelper.d("========before_setImpl=========");
	}
	
	public void before_cancel(MethodPlugParam param) {
		LogHelper.d("========before_cancel=========");
	}
}
