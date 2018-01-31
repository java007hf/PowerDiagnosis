package com.tencent.powerhook;

import java.lang.reflect.Method;
import java.util.Map;

import android.text.TextUtils;

import com.tencent.powerhook.Component.ComponentMethod;
import com.tencent.powerhook.TosPlugMethod.MethodPlugParam;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;


public class HookCallBack {
	private static final String TAG = "HookCallBack";
	/**
	 * 在这里实现你自己的逻辑，该方法会在原生方法执行那个之前
	 * 执行
	 * @param param 原生方法的对象
	 */
	public void callAllBeforePlugFuncs(MethodHookParam param, Map<String, Object> hookClasses){
		findAndInvokePlugFunc(param, "before", false, hookClasses);
	}
	
	/**
	 * 在这里实现你自己的逻辑，该方法会在原生方法执行那个之后
	 * 执行 
	 * @param param
	 */
	public void callAllAfterPlugFuncs(MethodHookParam param, Map<String, Object> hookClasses){
		//PlugLog.e("slide", "after(MethodHookParam param,Ma");
		
		findAndInvokePlugFunc(param, "after", false, hookClasses);
	}
	

	/**
	 * 查找匹配的方法并调用
	 * @param param
	 * @param invokeType
	 * @param needReturn
	 * @param hookClasses
	 */
	private void findAndInvokePlugFunc(MethodHookParam hookparam, String invokeType, boolean needReturn, Map<String, Object> hookClasses){
//		Log.d(TAG, "findAndInvokePlugFunc invokeType=" + invokeType + ", " + hookparam.method.getName());
		boolean isStaticFunc = false;
		
		if(hookClasses == null || hookClasses.size() <= 0){
			return;
		}
		
		if(ComponentReader.mHookClasses.size() > 0) {
			
			String hookedClassName = null;
			if (hookparam.thisObject != null) {
				hookedClassName = hookparam.thisObject.getClass().getName();
				LogHelper.d("tmphookClassName = " + hookedClassName);
			} else {
				//static hooked method, thisObject = null
				LogHelper.d("thisObject = null hookparam.method = " + hookparam.method.getName());
				isStaticFunc = true;
			}
			
			String theImpClassName = null;
			Class<?> lastTargetClass = null;
			for(Component tmpCp : ComponentReader.mComponents) {
				if (tmpCp.getTargetClassName().equals(hookedClassName)) {
					theImpClassName = tmpCp.getImpClassName();
					LogHelper.d("tmpCp.getTargetClassName().equals(tmphookClassName) theImpClassName = " + theImpClassName);
					break;
				} 
			}
			
			if (theImpClassName == null && hookparam.thisObject != null) {
				Class<?> theHookedClass = hookparam.thisObject.getClass();
				
				for(Component tmpCp : ComponentReader.mComponents) {
					try {
						Class<?> tmpTargetClass = Class.forName(tmpCp.getTargetClassName());
						if (tmpTargetClass.isAssignableFrom(theHookedClass)) {
							//tmpTargetClass is parent, theHookedClass is son
							//PlugLog.d(tmpTargetClass + " ==isAssignableFrom== " + theHookedClass + ", " + tmpCp.getMethods().size());
							//List<ComponentMethod> tmpMethods = ;
							String tmpImpCN = null;
							for (ComponentMethod tmpMtd : tmpCp.getMethods()) {
								if (tmpMtd.getMethodName().equals(hookparam.method.getName())) {
									tmpImpCN = tmpCp.getImpClassName();
									//PlugLog.d(tmpImpCN + " ==hasMethod== " + hookedClassName);
									break;
								}
							}
							
							if (tmpImpCN != null) {
								if (theImpClassName != null) {
									//Class<?> tmpCl = Class.forName(tmpImpCN);
									//Class<?> theCl = Class.forName(theImpClassName);
									if (lastTargetClass.isAssignableFrom(tmpTargetClass)) {
										//lastTargetClass is parent, tmpTargetClass is son
										//fix bug: onTouchEvent, find the litter son
										LogHelper.d(lastTargetClass + " isAssignableFrom exchange " + tmpTargetClass);
										theImpClassName = tmpImpCN;
										lastTargetClass = tmpTargetClass;
									}
								} else {
									theImpClassName = tmpImpCN;
									lastTargetClass = tmpTargetClass;
								}
							}
							
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
					
				}
				
			}
			
			//general func
			if (!isStaticFunc && theImpClassName != null) {
				invokeHookedFunc(hookparam, invokeType, needReturn, theImpClassName, hookClasses);
			} else {
				//static func, search, maybe error!(two same static functions in different class)!
				//better solution: set callback for each static hooked method
				for(String tmpHCN : ComponentReader.mHookClasses) {
					invokeHookedFunc(hookparam, invokeType, needReturn, tmpHCN, hookClasses);
				}
				
			}
			
		}
			
	}

	public int getSliptIndex(String invokeType){
		if("after".equals(invokeType)){
			return 6;
		}else if("before".equals(invokeType)){
			return 7;
		}
		return 0;
	}
	
	private void invokeHookedFunc(MethodHookParam hookparam, String invokeType, boolean needReturn, String tmpImpClassName, Map<String, Object> hookClasses){
		int sliptStart = getSliptIndex(invokeType);
		//Log.d(TAG, "invoke hookparam.method = " + hookparam.method.getName() + ", tmpImpClassName = " + tmpImpClassName);
		try {
			Object hookClassPlug = (Object) hookClasses.get(tmpImpClassName);
			if(hookClassPlug == null){
				LogHelper.d("hookClassPlug == null, tmpImpClassName = " + tmpImpClassName);
				return;
			}
			Class<?> clz = hookClassPlug.getClass();
			if(clz != null) {
				Method[] methods = clz.getDeclaredMethods();
				if(methods != null && methods.length > 0){
					for(Method method : methods){
						String methodName = method.getName();
						
						//bug, fredy modify 0701
						//String methodNameSuffix = methodName.substring(sliptStart, methodName.length());
						String methodNameSuffix = null;
						if (methodName.length() > sliptStart) {
							methodNameSuffix = methodName.substring(sliptStart, methodName.length());
						} else {
							continue;
						}
						
						String oriMethodName = hookparam.method.getName();
						LogHelper.d("invoke methodName="+methodName+"," + methodNameSuffix + ", "
								+ oriMethodName + ", " + invokeType + " method.getName() = " + method.getName());
						
						if(!TextUtils.isEmpty(methodName) && methodName.startsWith(invokeType)
								&& oriMethodName.equals(methodNameSuffix)) {
							LogHelper.d("call method.invoke param=" + hookparam + ", methodName=" + method.getName());
							
							//fredyfang modify!!!
							//method.invoke(hook, param);
							method.invoke(hookClassPlug, hookParam2plugParam(hookparam));
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
//	@Deprecated
//	public String getClassName(PlugUtils hook){
//		if(hook == null){
//			return null;
//		}
//		return hook.getClass().getName();
//	}
	
	
	
	public MethodPlugParam hookParam2plugParam(MethodHookParam hookParam) {
		//thisObject, args, result, Throwable???, method???
		MethodPlugParam plugParam = new MethodPlugParam();
		
		//PlugLog.d("enter hookParam2plugParam ... hookParam = " + hookParam);
//		int paramNum = hookParam.args.length;
//		plugParam.args = new Object[paramNum];
//		
//		for (int i = 0; i < paramNum; i++) {
//			plugParam.args[i] = hookParam.args[i];
//		}
		plugParam.args = hookParam.args;
		
		plugParam.thisObject = hookParam.thisObject;
		
		//plugParam.invokeType (don't need, known before call thi func)
//		if (hookParam.getResult() != null) {
//			plugParam.setResult(hookParam.getResult());
//		}
		
		plugParam.result = hookParam.getResult();
		plugParam.throwable = hookParam.getThrowable();
		
		LogHelper.d("hookParam2plugParam " + hookParam.method.getName() + ", " + hookParam.hashCode());
		if (plugParam.result != null) {
			LogHelper.d("hookParam2plugParam " + plugParam.result.hashCode() +  ", " + hookParam.getResult().hashCode());
		}
		
		plugParam.method = hookParam.method;
		
		plugParam.xposedHookParam = hookParam;
		
		return plugParam;
		
		
	}
	
}
