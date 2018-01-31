package com.tencent.qrom.powerdiagnosis.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.R.string;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tencent.qrom.powerdiagnosis.common.Constants;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsInfo;
import com.tencent.qrom.powerdiagnosis.utils.Helpers;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;
import com.tencent.qrom.powerdiagnosis.utils.XposedHelper;

public class ProcessTraceManager {
	private static final String TRACE_MODULE_PATH = "process.jar";
	private static final String TRACE_MODULE_CLASS = "com.tencent.powerhook.Main";
	private static final int MSG_TRACE_START_REQUEST = 1;
	private static final int MSG_TRACE_START_RESULT = 2;
	private static final int MSG_TRACE_STOP_REQUEST = 3;
	private static final int MSG_TRACE_STOP_RESULT = 4;
	private static final int MSG_TRACE_CLEAR_REQUEST = 7;
	private static final int MSG_TRACE_CLEAR_RESULT = 8;
	private static final int MSG_TRACE_GENERAL_REQUEST = 9;
	private static final int MSG_TRACE_GENERAL_RESULT = 10;
	
	public static final String GENERAL_COMMAND_KEY = "COMMAND";
	public static final String COMMAND_CLEARFILE = "CLEARFILE";
	public static final String COMMAND_FILELIST = "FILELIST";
	
	private static ProcessTraceManager instance;
	private Context mContext;
    private ServiceHandler mHandler;
    private Object mLock = new Object();
    private Set<ProcessTraceObserver> mSetObserver = new HashSet<ProcessTraceObserver>();
    private List<ProcessTraceItem> mProcessTraceList = new ArrayList<ProcessTraceItem>();
    private IntentFilter mResultFilter;
    private Boolean mWorking = false;
    private String mModulePath;

	public static ProcessTraceManager getInstance(Context context) {
		if(instance == null) {
			synchronized (ProcessTraceManager.class) {
				if(instance == null) {
					instance = new ProcessTraceManager(context.getApplicationContext());
				}
			}
		}
		return instance;
	}

	private ProcessTraceManager(Context context) {
		mContext = context;
        HandlerThread thread = new HandlerThread("PackageTracerService");
        thread.start();
        mHandler = new ServiceHandler(thread.getLooper());
    	mResultFilter = new IntentFilter();
    	mResultFilter.addAction(Constants.ACTION_PROCESS_START_RESULT);
    	mResultFilter.addAction(Constants.ACTION_PROCESS_STOP_RESULT);
    	mResultFilter.addAction(Constants.ACTION_PROCESS_CLEAR_RESULT);
    	mResultFilter.addAction(Constants.ACTION_PROCESS_GENERAL_RESULT);
        mModulePath = XposedHelper.TMP_FOLDER + "/" + TRACE_MODULE_PATH;
        File fileModule = new File(mModulePath);
        if(!fileModule.exists()) {
        	mModulePath = context.getCacheDir().getAbsolutePath() + File.separator + TRACE_MODULE_PATH;
        	Helpers.extractAssetsToLocal(mContext, TRACE_MODULE_PATH, mModulePath);
        }
    	LogHelper.v("ProcessTraceService running");
	}
	
	private void printRunningAppProcessInfo(RunningAppProcessInfo runningAppProcessInfo) {
		LogHelper.d("===processName " + runningAppProcessInfo.processName);
		LogHelper.d("pid " + runningAppProcessInfo.pid);
		LogHelper.d("uid " + runningAppProcessInfo.uid);
		
		for(String pkg : runningAppProcessInfo.pkgList) {
			LogHelper.d(pkg);
		}
	}
	
	public class ProcessNameComparator implements Comparator<ProcessTraceItem> {
		@Override
		public int compare(ProcessTraceItem m, ProcessTraceItem n) {
			String p1 = m.getInfo().processName;
			String p2 = n.getInfo().processName;
			return p1.compareToIgnoreCase(p2);
		}
	}

	private void updateItemStatus(ProcessTraceItem p) {
		if (p == null) return;
		SharedPreferences settings = mContext.getSharedPreferences(
				ProcessTraceActivity.SHARED_PREFENCE_TAG, Context.MODE_PRIVATE);
		LogHelper.d("settings = " + settings);
		LogHelper.d("p = " + p);
		LogHelper.d("p.getInfo() = " + p.getInfo());
		
		String rString = settings.getString(p.getInfo().processName, "");
		LogHelper.d(p.getInfo().processName);
		LogHelper.d("rString " + rString);
		
		if (!rString.equals("")) {
			String [] strings = rString.split("_");
			LogHelper.d("11111 " + strings[0] + " " + strings[1] + " " + p.getInfo().pid);
			if (strings[0].equals(p.getInfo().pid+"")) {
				LogHelper.d("11111 222 " + strings[1].equals("true"));
				p.setChecked(strings[1].equals("true"));
			}
		}
	}
	
	public List<ProcessTraceItem> updateProcessTraceList() {		
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processList = am.getRunningAppProcesses();
		List<ProcessTraceItem> processTraceList = new ArrayList<ProcessTraceItem>();
		for (RunningAppProcessInfo process : processList) {
//			printRunningAppProcessInfo(process);
			
			ProcessTraceItem item = findProcessItem(process.uid, process.pid);
			if (item == null && process.pid != android.os.Process.myPid()) {
				item = new ProcessTraceItem();
				item.setInfo(process);
				item.setChecked(false);
			}
			
			if (item != null) {
				processTraceList.add(item);
			}
			
			updateItemStatus(item);
		}
		mProcessTraceList.clear();
		mProcessTraceList.addAll(processTraceList);
		
		ProcessNameComparator p = new ProcessNameComparator();
		Collections.sort(mProcessTraceList, p);
		
		return mProcessTraceList;
	}

	public ProcessTraceItem findProcessItem(int uid, int pid) {
		for (ProcessTraceItem item : mProcessTraceList) {
			if (item.getInfo().uid == uid &&
					item.getInfo().pid == pid) {
				return item;
			}
		}
		return null;
	}

	public interface ProcessTraceObserver {
		void onTraceStartResult(ProcessTraceItem item, Bundle more);
		void onTraceStopResult(ProcessTraceItem item, Bundle more);
		void onTraceClearResult(ProcessTraceItem item, Bundle more);
		void onTraceGeneralResult(ProcessTraceItem item, Bundle more);
	}
	
    public void attach(ProcessTraceObserver observer){
		synchronized (mLock) {
			if(observer != null && !mSetObserver.contains(observer)) {
				mSetObserver.add(observer);
			}
			if (!mWorking && !mSetObserver.isEmpty()) {
				LogHelper.v("ProcessTraceManager.start working");
				mContext.registerReceiver(mReceiver, mResultFilter, null, mHandler);
				mWorking = true;
			}
		}
    }

	public void detach(ProcessTraceObserver observer) {
		synchronized (mLock) {
			if(observer != null && mSetObserver.contains(observer)) {
				mSetObserver.remove(observer);
			}
			if (mWorking && mSetObserver.isEmpty()) {
				LogHelper.v("ProcessTraceManager.stop working");
				mContext.unregisterReceiver(mReceiver);
				mWorking = false;
			}
		}
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogHelper.v("ProcessTraceManager.onReceive:" + intent);
			if(Constants.ACTION_PROCESS_START_RESULT.equals(intent.getAction())) {
				int uid = intent.getIntExtra(Constants.EXTRA_UID, 0);
				int pid = intent.getIntExtra(Constants.EXTRA_PID, 0);
				Bundle more  = intent.getParcelableExtra(Constants.EXTRA_MORE);
				ProcessTraceItem item = ProcessTraceManager.getInstance(mContext).findProcessItem(uid,pid);
				ProcessTraceManager.getInstance(mContext).startTraceResult(item, more);
			} else if(Constants.ACTION_PROCESS_STOP_RESULT.equals(intent.getAction())) {
				int uid = intent.getIntExtra(Constants.EXTRA_UID, 0);
				int pid = intent.getIntExtra(Constants.EXTRA_PID, 0);
				Bundle more  = intent.getParcelableExtra(Constants.EXTRA_MORE);
				ProcessTraceItem item = ProcessTraceManager.getInstance(mContext).findProcessItem(uid,pid);
				ProcessTraceManager.getInstance(mContext).stopTraceResult(item, more);
			} else if(Constants.ACTION_PROCESS_CLEAR_RESULT.equals(intent.getAction())) {
				int uid = intent.getIntExtra(Constants.EXTRA_UID, 0);
				int pid = intent.getIntExtra(Constants.EXTRA_PID, 0);
				Bundle more  = intent.getParcelableExtra(Constants.EXTRA_MORE);
				ProcessTraceItem item = ProcessTraceManager.getInstance(mContext).findProcessItem(uid,pid);
				ProcessTraceManager.getInstance(mContext).clearTraceResult(item, more);
			} else if(Constants.ACTION_PROCESS_GENERAL_RESULT.equals(intent.getAction())) {
				int uid = intent.getIntExtra(Constants.EXTRA_UID, 0);
				int pid = intent.getIntExtra(Constants.EXTRA_PID, 0);
				Bundle more  = intent.getParcelableExtra(Constants.EXTRA_MORE);
				ProcessTraceItem item = ProcessTraceManager.getInstance(mContext).findProcessItem(uid,pid);
				ProcessTraceManager.getInstance(mContext).generalTraceResult(item, more);
			}	
		}
	};
	
	public void startTraceRequest(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.startTraceRequest");
			sendMessage(MSG_TRACE_START_REQUEST, item, more);
		}
	}

	public void startTraceResult(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.startTraceResult " + item);
			sendMessage(MSG_TRACE_START_RESULT, item, more);
		}
	}
	
	public void stopTraceRequest(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.stopTraceRequest " + item);
			sendMessage(MSG_TRACE_STOP_REQUEST, item, more);
		}
	}

	public void stopTraceResult(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.stopTraceResult " + item);
			sendMessage(MSG_TRACE_STOP_RESULT, item, more);
		}
	}

	public void clearTraceRequest(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.clearTraceRequest" + item);
			sendMessage(MSG_TRACE_CLEAR_REQUEST, item, more);
		}
	}

	public void clearTraceResult(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.clearTraceResult" + item);
			sendMessage(MSG_TRACE_CLEAR_RESULT, item, more);
		}
	}

	public void generalTraceRequest(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.generalTraceRequest" + item);
			sendMessage(MSG_TRACE_GENERAL_REQUEST, item, more);
		}
	}
	
	public void generalTraceResult(ProcessTraceItem item, Bundle more) {
		if (item != null && mProcessTraceList.contains(item)) {
			LogHelper.v("ProcessTraceManager.generalTraceResult" + item);
			sendMessage(MSG_TRACE_GENERAL_RESULT, item, more);
		}
	}

	private void sendMessage(int what, ProcessTraceItem item, Bundle more) {
		if (item == null) return;
		Message msg = mHandler.obtainMessage(what);
		msg.arg1 = item.getInfo().uid;
		msg.arg2 = item.getInfo().pid;
		msg.obj = more;
		mHandler.sendMessage(msg);
	}

	private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
		
		@Override
		public void handleMessage(Message msg) {	
			switch (msg.what) {
			case MSG_TRACE_START_REQUEST:
				handleTraceStartRequest(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_START_RESULT:
				handleTraceStartResult(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_STOP_REQUEST:
				handleTraceStopRequest(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_STOP_RESULT:
				handleTraceStopResult(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_CLEAR_REQUEST:
				handleTraceClearRequest(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_CLEAR_RESULT:
				handleTraceClearResult(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_GENERAL_REQUEST:
				handleTraceGeneralRequest(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			case MSG_TRACE_GENERAL_RESULT:
				handleTraceGeneralResult(findProcessItem(msg.arg1, msg.arg2), (Bundle)msg.obj);
				break;
			default:
				break;
			}
		}
	}
	
	private void handleTraceStartRequest(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceStartRequest" + item);
		if (item == null ) return;
		
		boolean isHook = XposedHelper.isHooked(item.getInfo().pid);
		
		if (isHook) {
			LogHelper.v("ProcessTraceManager.handleTraceStartRequest: already hook");
			sendRequestBroadcast(Constants.ACTION_PROCESS_START_REQUEST, item, more);
		} else {
			LogHelper.v("ProcessTraceManager.handleTraceStartRequest: do hook");
			String processName = item.getInfo().processName;
			if (processName.equals("system")) {
				processName = "system_server";
			}
			XposedHelper.hook(item.getInfo().pkgList[0], processName, mModulePath, TRACE_MODULE_CLASS);
		}
    }
    
	private void handleTraceStartResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceStartResult" + item);
		if (item == null) return;
		item.setChecked(true);
		if(mProcessTraceList.contains(item)) {
			synchronized (mLock) {
				for(ProcessTraceObserver observer : mSetObserver) {
					observer.onTraceStartResult(item, more);
				}
			}
		}
    }
	
	private void handleTraceStopRequest(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceStopRequest" + item);
		if (item == null) return;
		sendRequestBroadcast(Constants.ACTION_PROCESS_STOP_REQUEST, item, more);
    }
    
	private void handleTraceStopResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceStopResult" + item);
		if (item == null) return;
		if(mProcessTraceList.contains(item)) {
			synchronized (mLock) {
				for(ProcessTraceObserver observer : mSetObserver) {
					observer.onTraceStopResult(item, more);
				}
			}
		}
    }

	private void handleTraceClearRequest(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceClearRequest" + item);
		if (item == null) return;
		sendRequestBroadcast(Constants.ACTION_PROCESS_CLEAR_REQUEST, item, more);
    }
    
	private void handleTraceClearResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceClearResult" + item);
		if (item == null) return;
		if(mProcessTraceList.contains(item)) {
			synchronized (mLock) {
				for(ProcessTraceObserver observer : mSetObserver) {
					observer.onTraceClearResult(item, more);
				}
			}
		}
    }
	
	private void handleTraceGeneralRequest(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceGeneralRequest" + item);
		if (item == null) return;
		sendRequestBroadcast(Constants.ACTION_PROCESS_GENERAL_REQUEST, item, more);
    }
    
	private void handleTraceGeneralResult(ProcessTraceItem item, Bundle more) {
		LogHelper.v("ProcessTraceManager.handleTraceGeneralResult" + item);
		if (item == null) return;
		if(mProcessTraceList.contains(item)) {
			synchronized (mLock) {
				for(ProcessTraceObserver observer : mSetObserver) {
					observer.onTraceGeneralResult(item, more);
				}
			}
		}
    }
	
	private void sendRequestBroadcast(String action, ProcessTraceItem item, Bundle more) {
		if (item == null) return;
		for (String pkg : item.getInfo().pkgList) {
			Intent intent = new Intent().setPackage(pkg);
			intent.setAction(action);
			intent.putExtra(Constants.EXTRA_UID, item.getInfo().uid);
			intent.putExtra(Constants.EXTRA_PID, item.getInfo().pid);
			if (more != null) intent.putExtra(Constants.EXTRA_MORE, more);
			mContext.sendBroadcast(intent);
		}
	}
}
