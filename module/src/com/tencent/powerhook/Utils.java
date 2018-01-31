package com.tencent.powerhook;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Application;

import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

import dalvik.system.DexFile;

public class Utils {
	public final static String TAG = "ClassUtil";
	
	private static ArrayList<String>  mExcludePkg = new ArrayList<String>();
	static{
		//launcher
		mExcludePkg.add("com.tencent.qrom.account.sdk");
		mExcludePkg.add("qrom.component.statistic");
		mExcludePkg.add("OPT");
		mExcludePkg.add("TRom");
		mExcludePkg.add("android.support");
		mExcludePkg.add("qrom.component");
		mExcludePkg.add("oicq.wlogin_sdk");
		mExcludePkg.add("com.tencent.feedback");
		
		//dm
		mExcludePkg.add("com.tencent.tws.qrom.support.v4");
		mExcludePkg.add("com.android.volley");
		mExcludePkg.add("com.google.gson");
		mExcludePkg.add("com.google.zxing");
		mExcludePkg.add("com.tencent.beacon");
		mExcludePkg.add("com.tencent.kingkong");
		mExcludePkg.add("com.tencent.map");
		mExcludePkg.add("com.tencent.mm");
		
		//keyguard
		mExcludePkg.add("org.ogre3d.android.RenderController");
		
		//systemserver
		mExcludePkg.add("com.android.server.wifi.WifiTrafficPoller");
		mExcludePkg.add("com.android.server.power.DisplayPowerController");
		mExcludePkg.add("com.android.server.DeviceStorageMonitorService");
		mExcludePkg.add("com.android.server.AlarmManagerService");
	}
	
	private static boolean needHook(String className) {
		for(String string : mExcludePkg) {
			boolean b = className.startsWith(string);
			if (b) return false;
		}
		
		return true;
	}

	public synchronized static List<Class<?>> getClassList(ClassLoader loader, boolean isSystem, String classPrefix) {
		LogHelper.d("getClassList isSystem = " + isSystem);
		int countClazz = 0;
		int countMethod = 0;
		List<Class<?>> classList = new ArrayList<Class<?>>();
		try {
			DexFile df = null;
			if (!isSystem) {
				df = getDexFile(loader);
			} else {
				df = new DexFile("/data/local/tmp/systemlog/services.jar");
			}
			
			Field mCookieField = findField(df, "mCookie");
			Object mCookieObj = mCookieField.get(df);
			
			int version = Integer.parseInt(android.os.Build.VERSION.SDK);
			LogHelper.d("version = " + version);
			Method getClassNameListMethod = null;
			if (version <= 19) {
				getClassNameListMethod = findMethod(df, "getClassNameList", int.class);
			} else {
				getClassNameListMethod = findMethod(df, "getClassNameList", long.class);
			}
			String[] strList = (String[])getClassNameListMethod.invoke(df, mCookieObj);
			
			for (String className : strList) {
				try {
					if (needHook(className)) {
						LogHelper.d("className : " + className);
						Class<?> clazz = loader.loadClass(className);
						if (clazz != null && !clazz.isInterface()) {
							classList.add(clazz);
							countClazz++;
							LogHelper.d(className);
							Method[] methods = clazz.getDeclaredMethods();
							for (Method method : methods) {
								LogHelper.d("------" + method.getName());
								countMethod++;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Error e) {
					e.printStackTrace();
				}
				
				LogHelper.d("=========end=========\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogHelper.d("class total:" + countClazz + ", method total:" + countMethod);
		return classList;
	}

	public static String printStack(Throwable throwable) {
		String s = null;
		StackTraceElement[] stackTraceElements = getStackTrace(throwable);
		if (stackTraceElements != null) {
			s = printStack(stackTraceElements);
		}
		
		return s;
	}
	
	public static StackTraceElement[] getStackTrace(Throwable throwable) {
		try {
			Class clazz = throwable.getClass(); 
			Method m1 = clazz.getDeclaredMethod("getInternalStackTrace");
			m1.setAccessible(true);
			StackTraceElement[] element = (StackTraceElement[]) m1.invoke(throwable); 
			return element;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
	
	public static String printStack(StackTraceElement stack[]) {
		int i;
		StringBuilder sb = new StringBuilder();
		String lastString = "";
		String currentString = "";
		for (i = 0; i < stack.length; i++) {
			StackTraceElement ste = stack[i];
			String fileNameString = ste.getFileName();
			
			if (fileNameString == null
					|| fileNameString.equals("XposedBridge.java")
					|| fileNameString.equals("AlarmManagerHook.java")
					|| fileNameString.equals("CPUHook.java")
					|| fileNameString.equals("LocationManagerHook.java")
					|| fileNameString.equals("LegacySensorManagerHook.java")
					|| fileNameString.equals("SystemSensorManagerHook.java")
					|| fileNameString.equals("MediaScanHook.java")
					|| fileNameString.equals("WakeLockHook.java")
					|| fileNameString.equals("WifiManagerHook.java")
					|| fileNameString.equals("HookCallBack.java")
					|| fileNameString.equals("BroadcastHook.java")
					|| fileNameString.equals("ProcessHook.java")
					|| fileNameString.equals("Method.java")
					|| fileNameString.equals("CPUHook.java")) continue;
			
			currentString = ste.getClassName() + "." + ste.getMethodName() + "(" + fileNameString + ":";
			sb.append(currentString);
			if (currentString.equals(lastString) && ste.getLineNumber() == -2) {
				lastString = currentString;
				continue;
			}
			lastString = currentString;
			sb.append(ste.getLineNumber());
			sb.append(")\n");
		}
		
		return sb.toString();
	}
	
	public static Application getCurrentApplication() {
		Application application = null;
		try {
		    final Class<?> activityThreadClass =
		            Class.forName("android.app.ActivityThread");
		    final Method method = activityThreadClass.getMethod("currentApplication");
		    application = (Application) method.invoke(null, (Object[]) null);
		    LogHelper.d("application = " + application);
		} catch (Exception e) {
			LogHelper.d("error = " + e);
			e.printStackTrace();
		}
		return application;
	}

	public static String getTime(long m) {
		Date date = new Date(m);
		SimpleDateFormat sdFormatter = new SimpleDateFormat("MM-dd_HH-mm-ss");  
		String retStrFormatNowDate = sdFormatter.format(date);
		return retStrFormatNowDate;
	}
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
    
    public static String methodToString(Method method) {
    	Class<?>[] types = method.getParameterTypes();
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append(method.getDeclaringClass().getName());
		sBuilder.append(".");
		sBuilder.append(method.getName());
		sBuilder.append("(");
		for (int i=0;i<types.length;i++) {
			if (i!=0) sBuilder.append(",");
			sBuilder.append(types[i].getName());
		}
		sBuilder.append(")");
		
		return sBuilder.toString();
    }
    
    private static DexFile getDexFile(ClassLoader loader) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
    	DexFile dexFile = null;
    	Field pathListField = findField(loader, "pathList");
        Object dexPathList = pathListField.get(loader);
        ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
        
        Field dexElementsField = findField(dexPathList, "dexElements");
        Object[] dexElements = (Object[]) dexElementsField.get(dexPathList);
        
        for(Object elementObject : dexElements) {
        	Field dexFileField = findField(elementObject, "dexFile");
        	dexFile = (DexFile) dexFileField.get(elementObject);
        	LogHelper.d("dexFile elementObject = " + elementObject);
        	LogHelper.d("dexFile getName = " + dexFile.getName());
        }
        
        return dexFile;
    }
    
    public static byte[] toByteArray(int iSource) {
        byte[] bLocalArr = new byte[4];
        for (int i = 0; i < 4; i++) {
            bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);
        }
        return bLocalArr;
    }
    
    public static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }
}