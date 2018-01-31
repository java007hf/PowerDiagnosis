package com.tencent.powerhook;

import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;

public interface TosPlugInterface {
	public void invoke(String method_name, int prefix, MethodPlugParam param);
}
