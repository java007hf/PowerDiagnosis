package com.tencent.powerhook.hookfun;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.tencent.powerhook.PlugUtils;
import com.tencent.powerhook.ProcessHook;
import com.tencent.powerhook.ProcessStatsHelper;
import com.tencent.powerhook.Utils;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class CPUHook {
	private static CPUHook mCpuHook = null;
	private int mHookSize = 0;
	private static final int MAX_HOOK_SIZE_4 = 25000;
	private static final int MAX_HOOK_SIZE_5 = 15000;
	private static int mMax = MAX_HOOK_SIZE_5;

	private CPUHook() {

	}

	public static CPUHook getInstance() {
		if (null == mCpuHook) {
			mCpuHook = new CPUHook();
		}
		
		String version = PlugUtils.getCurAndroidVersion();
		if (version.startsWith("5")) {
			mMax = MAX_HOOK_SIZE_5;
		} else if (version.startsWith("4")) {
			mMax = MAX_HOOK_SIZE_4;
		} else {
			mMax = MAX_HOOK_SIZE_5;
		}

		return mCpuHook;
	}

	public void hook(LoadPackageParam lpparam, boolean isSystem) {
		if (isSystem && ProcessHook.CURRENT_OS_VERSION <= 19) {
			LogHelper.d("android 4.4 can not hook system!!!");
			return;
		}
		
		// /////////////////////
		// HOOK ALL METHOD!!!
		// ////////////////////
		List<Class<?>> classes = Utils.getClassList(lpparam.classLoader,
				isSystem, "");

		mHookSize = 0;
		for (Class<?> class1 : classes) {
			if (!hookClass(class1)) break;
		}
	}

	private boolean hookClass(Class<?> class1) {
		LogHelper.d("hook class1 = " + class1.getName());
		try {
			Method[] methods = class1.getDeclaredMethods();

			for (Method method : methods) {
				if (Modifier.isAbstract(method.getModifiers()))
					continue;
				if (Modifier.isNative(method.getModifiers()))
					continue;
				LogHelper.d("hook method = " + method.getName());

				if (mHookSize < mMax) {
					mHookSize++;
					LogHelper.d("mHookSize = " + mHookSize);
				} else {
					LogHelper.d("HOOK IS FULL !!!");
					return false;
				}
				Class<?>[] types = method.getParameterTypes();
				switch (types.length) {
				case 0:
					XposedHelpers.findAndHookMethod(class1, method.getName(),
							new MyXC());
					break;
				case 1:
					XposedHelpers.findAndHookMethod(class1, method.getName(),
							types[0], new MyXC());
					break;
				case 2:
					XposedHelpers.findAndHookMethod(class1, method.getName(),
							types[0], types[1], new MyXC());
					break;
				case 3:
					XposedHelpers.findAndHookMethod(class1, method.getName(),
							types[0], types[1], types[2], new MyXC());
					break;
				case 4:
					XposedHelpers.findAndHookMethod(class1, method.getName(),
							types[0], types[1], types[2], types[3], new MyXC());
					break;
				case 5:
					XposedHelpers.findAndHookMethod(class1, method.getName(),
							types[0], types[1], types[2], types[3], types[4],
							new MyXC());
					break;
				default:
					break;
				}
			}
		} catch (NoClassDefFoundError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;
	}

	class MyXC extends XC_MethodHook {
		@Override
		protected void beforeHookedMethod(MethodHookParam param)
				throws Throwable {
			ProcessStatsHelper.getInstance().noteStartMethod(param,
					new Throwable());
			super.beforeHookedMethod(param);
		}

		@Override
		protected void afterHookedMethod(MethodHookParam param)
				throws Throwable {
			ProcessStatsHelper.getInstance().noteStopMethod(param);
			super.afterHookedMethod(param);
		}
	}
}
