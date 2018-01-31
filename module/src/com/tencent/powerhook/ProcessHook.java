package com.tencent.powerhook;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.ServiceManager;

import com.android.server.am.ActivityManagerService;
import com.tencent.powerhook.FrameworkApiList.PlugMethod;
import com.tencent.powerhook.hookfun.CPUHook;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;
import com.tencent.qrom.powerdiagnosis.utils.ProcessTraceClient;
import com.tencent.qrom.powerdiagnosis.utils.ProcessTraceClient.ProcessTraceObserver;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ProcessHook implements IXposedHookLoadPackage, ProcessTraceObserver {
	public static final String HOOK_PACKAGENAME = "com.tencent.qrom.powerdiagnosis";
	public static final Object SYNC_LOCK = new Object();
	
	private static final String THREADNAME = "HOOK_THREAD";
	private HookCallBack mHookFuncs;
	private Map<String, Object> mHooks;
	
	public static final String ACTION_DUMP_ALL_HOOKINFO = "DUMP_ALL_HOOKINFO";
	public static final String ACTION_CLEAR_ALL_HOOKINFO = "CLEAR_ALL_HOOKINFO";
	
	private static final String PROFILE_PATH = "/data/powersave/";
	private ProcessTraceClient mProcessTraceClient = null;
	private Context mContext;
	private long mStartRecordTime = 0;
	private long mStopRecordTime = 0;
	private boolean mIsSystem = false;
	private Handler mHandler;
	public static int CURRENT_OS_VERSION = android.os.Build.VERSION.SDK_INT;
	private static final int MSG_STOP_REQUEST = 0x01;
	private static final int MSG_HOOK_METHOD = 0x02;
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		XposedBridge.log("handleLoadPackage package = " + lpparam.packageName);
		//init
		HandlerThread handlerThread = new HandlerThread(THREADNAME);
		handlerThread.start();
		mHandler = new MyHandler(handlerThread.getLooper());
		
		//init hook info
		ProcessStatsHelper processStatsHelper = ProcessStatsHelper.getInstance();
		processStatsHelper.setPackageName(lpparam.packageName);
		processStatsHelper.setProcessName(lpparam.processName);
		processStatsHelper.setUid(Process.myUid());
		processStatsHelper.setPid(Process.myPid());
		
		mIsSystem = lpparam.packageName.equals("android");
		
		if (mIsSystem) {
			IBinder binder = ServiceManager.getService(Context.ACTIVITY_SERVICE);
			IActivityManager iActivityManager  = ActivityManagerNative.asInterface(binder);
			ActivityManagerService ams = (ActivityManagerService)iActivityManager;
			mContext = (Context)ReflectOptUtils.getFieldValue("mContext", ams, ActivityManagerService.class);
		} else {
			Context application = (Context) Utils.getCurrentApplication();
			mContext = application.getApplicationContext();
		}
		
		mProcessTraceClient = ProcessTraceClient.getInstance(mContext); 
		mProcessTraceClient.attach(this);
		
		//hook method by xml
		initHookable(lpparam, mContext);
		
		if (mHandler != null) {
			Message message = mHandler.obtainMessage(MSG_HOOK_METHOD);
			message.obj= lpparam;
			mHandler.sendMessage(message);
		} else {
			startHookMethod(lpparam, mIsSystem);
		}
	}
	
	private void startHookMethod(LoadPackageParam lpparam, boolean isSystem) {
		CPUHook.getInstance().hook(lpparam, isSystem);
		onTraceStartRequest(null);
	}

	private void initHookable(LoadPackageParam loadPackageParam, Context context) {
		FrameworkApiList.getInstance();
		mHookFuncs = new HookCallBack();
		PlugRegister.getInstance(context);
		if (ComponentReader.mHookClasses.size() > 0) {
			mHooks = new HashMap<String, Object>();
			for (String hookClassName : ComponentReader.mHookClasses) {
				try {
					Class<?> hookClz = Class.forName(hookClassName);
					if (hookClz != null) {
						Object hook = (Object) hookClz.newInstance();
						mHooks.put(hookClassName, hook);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		invokeTypeMethods(loadPackageParam, FrameworkApiList.INVOKE_TYPE_BEFORE, mHooks);
		invokeTypeMethods(loadPackageParam, FrameworkApiList.INVOKE_TYPE_AFTER, mHooks);
	}

	public String[] getHookClasses(int invokeType) {
		Set<String> keySet = null;

		switch (invokeType) {
		case FrameworkApiList.INVOKE_TYPE_BEFORE:
			keySet = FrameworkApiList.mInvokeBeforeOriMethods.keySet();
			break;
		case FrameworkApiList.INVOKE_TYPE_AFTER:
			keySet = FrameworkApiList.mInvokeAfterOriMethods.keySet();
			break;

		default:
			break;
		}

		String[] classNames = null;
		if (keySet != null && keySet.size() > 0) {
			classNames = new String[keySet.size()];
			Iterator<String> keyIterator = keySet.iterator();
			int keyIndex = 0;
			while (keyIterator.hasNext()) {
				classNames[keyIndex] = keyIterator.next();
				keyIndex++;
			}
		}
		return classNames;
	}

	private void invokeTypeMethods(LoadPackageParam lpparam, int invokeType,
			final Map<String, Object> hookClasses) {
		final String[] classes = getHookClasses(invokeType);

		// PlugLog.d("enter getOriMethods classes=" + classes);
		if (classes == null || classes.length == 0) {
			return;
		}

		synchronized (this) {
			for (int i = 0; i < classes.length; i++) {
				List<PlugMethod> methods = getMethods(invokeType, classes[i]);
				if (methods != null && methods.size() > 0) {
					for (PlugMethod m : methods) {
						String methodName = m.getMethodName();
						Object[] args = null;
						List<Class<?>> parameterType = m.getArgs();
						if ("void".equals(parameterType.get(0).getName())) {
							args = new Object[parameterType.size()];
						} else {
							args = new Object[parameterType.size() + 1];
						}

						for (int j = 0; j < parameterType.size(); j++) {
							args[j] = parameterType.get(j);
						}
						boolean isConstructor = m.isConstructor();
						if (invokeType == FrameworkApiList.INVOKE_TYPE_AFTER) {
							args[args.length - 1] = new XC_MethodHook() {

								@Override
								protected void afterHookedMethod(
										MethodHookParam param) throws Throwable {
									if (mHookFuncs != null) {
										mHookFuncs.callAllAfterPlugFuncs(param,
												hookClasses);
									}
								}
							};
						} else if (invokeType == FrameworkApiList.INVOKE_TYPE_BEFORE) {
							args[args.length - 1] = new XC_MethodHook() {

								@Override
								protected void beforeHookedMethod(
										MethodHookParam param) throws Throwable {
									if (mHookFuncs != null) {
										mHookFuncs.callAllBeforePlugFuncs(
												param, hookClasses);
									}
								}
							};
						}
						
						try {
							if (lpparam != null) {
								if (isConstructor) {
									XposedHelpers.findAndHookConstructor(
											classes[i], lpparam.classLoader, args);
								} else {
									XposedHelpers.findAndHookMethod(classes[i],
											lpparam.classLoader, methodName, args);
								}
							} else {
								if (isConstructor) {
									XposedHelpers.findAndHookConstructor(
											classes[i], null, args);
								} else {
									XposedHelpers.findAndHookMethod(classes[i],
											null, methodName, args);
								}
							}
						} catch (Error e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private List<PlugMethod> getMethods(int type, String className) {
		switch (type) {
		case FrameworkApiList.INVOKE_TYPE_AFTER:
			return FrameworkApiList.mInvokeAfterOriMethods.get(className);
		case FrameworkApiList.INVOKE_TYPE_BEFORE:
			return FrameworkApiList.mInvokeBeforeOriMethods.get(className);
		}
		return null;
	}

	@Override
	public void onTraceStartRequest(Bundle more) {
		LogHelper.d("====onTraceStartRequest====");
		mStartRecordTime = System.currentTimeMillis();
		Bundle bundle = new Bundle();
		bundle.putLong("STARTTIME", mStartRecordTime);
		mProcessTraceClient.startTraceResult(bundle);
		ProcessStatsHelper.getInstance().setCanNoteFlag(true);
	}

	@Override
	public void onTraceStopRequest(Bundle more) {
		LogHelper.d("====onTraceStopRequest====");
		if (mHandler != null) mHandler.sendEmptyMessage(MSG_STOP_REQUEST);
	}

	@Override
	public void onTraceClearRequest(Bundle more) {
		ProcessStatsHelper.getInstance().clear();
		mStartRecordTime = System.currentTimeMillis();
		Bundle bundle = new Bundle();
		bundle.putLong("STARTTIME", mStartRecordTime);
		mProcessTraceClient.clearTraceResult(bundle);
	}

	@Override
	public void onTraceGeneralRequest(Bundle more) {
		LogHelper.d("onTraceGeneralRequest 1111");
		if (more == null) return;
		String action = more.getString(ProcessTraceClient.GENERAL_COMMAND_KEY);
		if (ProcessTraceClient.COMMAND_CLEARFILE.equals(action)) {
			ProcessStatsHelper.getInstance().clearFile(mContext);
			Bundle bundle = new Bundle();
			bundle.putString(ProcessTraceClient.GENERAL_COMMAND_KEY, 
					ProcessTraceClient.COMMAND_CLEARFILE);
			mProcessTraceClient.generalTraceResult(bundle);
		} else if (ProcessTraceClient.COMMAND_FILELIST.equals(action)) {
			String[] fileStrings = ProcessStatsHelper.getInstance().getFileList(mContext);
			Bundle bundle = new Bundle();
			bundle.putString(ProcessTraceClient.GENERAL_COMMAND_KEY, 
					ProcessTraceClient.COMMAND_FILELIST);
			bundle.putStringArray("files", fileStrings);
			mProcessTraceClient.generalTraceResult(bundle);
		}
	}
	
	class MyHandler extends Handler {
		public MyHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_STOP_REQUEST:
				String path;
				
				if (mIsSystem) {
					path = "/data/local/tmp/systemlog" + "/process_info" + Utils.getTime(mStartRecordTime);
				} else {
					path = mContext.getCacheDir() + "/process_info" + Utils.getTime(mStartRecordTime);
				}
//				ProcessStatsHelper processStatsHelper = ProcessStatsHelper.getInstance();
//				String processName = processStatsHelper.getProcessName();
//				int pid = processStatsHelper.getPid();
//				String path = PROFILE_PATH + processName + "_" + pid 
//						+ "_" + Utils.getTime(mStartRecordTime);
				ProcessStatsHelper.getInstance().save(path);
				mStopRecordTime = System.currentTimeMillis();
				Bundle bundle = new Bundle();
				bundle.putLong("STOPTIME", mStopRecordTime);
				bundle.putString("PATH", path);
				mProcessTraceClient.stopTraceResult(bundle);
				break;
			case MSG_HOOK_METHOD:
				LoadPackageParam lpparam = (LoadPackageParam) msg.obj;
				startHookMethod(lpparam, mIsSystem);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
}
