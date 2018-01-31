package com.tencent.powerhook;

import android.content.Context;

//import de.robv.android.xposed.XposedBridge;
//import android.content.res.XResources;

public final class PlugRegister {
	public static Context mContext;

	private static PlugRegister mInstance;
	private PlugRegister(Context context) {
		mContext = context;
	}
	
	public static PlugRegister getInstance() {
		if(mInstance == null){
			mInstance = new PlugRegister(null);
			init(null);
		}
		return mInstance;
	}
	
	public static PlugRegister getInstance(Context context) {
		if(mInstance == null){
			mInstance = new PlugRegister(context);
			init(context);
		}
		return mInstance;
	}
	
	private static void init(Context context) {
		ComponentReader cptReader =  ComponentReader.getInstance();
		cptReader.readRegXmlAndParse(context);
	}
	
	/**
	 * 注册你需要修改的类，
	 * @param invokeType 方法调用时机
	 * @param targetClassName   目标类名
	 * @param methodName    目标方法名
	 * @param impClassName   实现类（替代类）
	 * @param methodArgsType  目标方法的参数类型（class）
	 */
	public void register(int minVersion,int maxVersion,int invokeType,String targetClassName,String methodName,String impClassName,boolean isConstructor, Class<?>... methodArgsType){
		FrameworkApiList.putHookMethod(minVersion,maxVersion,invokeType, targetClassName, methodName, isConstructor,methodArgsType);
		
	}
}
