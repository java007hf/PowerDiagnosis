package com.tencent.qrom.powerdiagnosis.utils;

import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tencent.qrom.powerdiagnosis.common.Constants;

public class ProcessTraceClient {
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
	
	private static ProcessTraceClient instance;
	private Context mContext;
	private ClientHandler mHandler;
	private Object mLock = new Object();
	private Set<ProcessTraceObserver> mSetObserver = new HashSet<ProcessTraceObserver>();

	public static ProcessTraceClient getInstance(Context context) {
		if(instance == null) {
			synchronized (ProcessTraceClient.class) {
				if(instance == null) {
					LogHelper.d("context = " + context);
					instance = new ProcessTraceClient(context);
				}
			}
		}
		return instance;
	}

	public ProcessTraceClient(Context context) {
		mContext = context;
        HandlerThread thread = new HandlerThread("ProcessTraceClient");
        thread.start();
        mHandler = new ClientHandler(thread.getLooper());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_PROCESS_START_REQUEST);
        filter.addAction(Constants.ACTION_PROCESS_STOP_REQUEST);
        filter.addAction(Constants.ACTION_PROCESS_CLEAR_REQUEST);
        filter.addAction(Constants.ACTION_PROCESS_GENERAL_REQUEST);
        mContext.registerReceiver(mReceiver, filter, null, mHandler);
	}

	public interface ProcessTraceObserver {
		void onTraceStartRequest(Bundle more);
		void onTraceStopRequest(Bundle more);
		void onTraceClearRequest(Bundle more);
		void onTraceGeneralRequest(Bundle more);
	}
	
    public void attach(ProcessTraceObserver observer){
		synchronized (mLock) {
			if(observer != null && !mSetObserver.contains(observer)) {
				mSetObserver.add(observer);
			}
		}
    }

	public void detach(ProcessTraceObserver observer) {
		synchronized (mLock) {
			if(observer != null && mSetObserver.contains(observer)) {
				mSetObserver.remove(observer);
			}
		}
	}
	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogHelper.v("ProcessTraceClient.onReceive:" + intent);
			if (!checkRequestIntentVaild(intent)) return;
			if(Constants.ACTION_PROCESS_START_REQUEST.equals(intent.getAction())) {
				Bundle more = intent.getParcelableExtra(Constants.EXTRA_MORE);
				startTraceRequest(more);
			} else if(Constants.ACTION_PROCESS_STOP_REQUEST.equals(intent.getAction())) {
				Bundle more = intent.getParcelableExtra(Constants.EXTRA_MORE);
				stopTraceRequest(more);
			} else if(Constants.ACTION_PROCESS_CLEAR_REQUEST.equals(intent.getAction())) {
				Bundle more = intent.getParcelableExtra(Constants.EXTRA_MORE);
				clearTraceRequest(more);
			} else if(Constants.ACTION_PROCESS_GENERAL_REQUEST.equals(intent.getAction())) {
				Bundle more = intent.getParcelableExtra(Constants.EXTRA_MORE);
				generalTraceRequest(more);
			}
		}

		private boolean checkRequestIntentVaild(Intent intent) {
			if (intent == null) return false;
			int uid = intent.getIntExtra(Constants.EXTRA_UID, 0);
			int pid = intent.getIntExtra(Constants.EXTRA_PID, 0);
			if (mContext.getApplicationInfo().uid == uid && android.os.Process.myPid() == pid)
				return true;
			return false;
		}
	};

	public void startTraceRequest(Bundle more) {		
		LogHelper.v("ProcessTraceClient.startTraceRequest");
		sendMessage(MSG_TRACE_START_REQUEST, more);
	}

	public void startTraceResult(Bundle more) {		
		LogHelper.v("ProcessTraceClient.startTraceResult");
		sendMessage(MSG_TRACE_START_RESULT, more);
	}

	public void stopTraceRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.stopTraceRequest");
		sendMessage(MSG_TRACE_STOP_REQUEST, more);
	}

	public void stopTraceResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.stopTraceResult");
		sendMessage(MSG_TRACE_STOP_RESULT, more);
	}

	public void clearTraceRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.clearTraceRequest");
		sendMessage(MSG_TRACE_CLEAR_REQUEST, more);
	}

	public void clearTraceResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.clearTraceResult");
		sendMessage(MSG_TRACE_CLEAR_RESULT, more);
	}

	public void generalTraceRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.generalTraceRequest");
		sendMessage(MSG_TRACE_GENERAL_REQUEST, more);
	}

	public void generalTraceResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.generalTraceResult");
		sendMessage(MSG_TRACE_GENERAL_RESULT, more);
	}

	private void sendMessage(int what, Bundle more) {
		Message msg = mHandler.obtainMessage(what);
		msg.obj = more;
		mHandler.sendMessage(msg);
	}

	private final class ClientHandler extends Handler {
        public ClientHandler(Looper looper) {
            super(looper);
        }
		
		@Override
		public void handleMessage(Message msg) {	
			switch (msg.what) {
			case MSG_TRACE_START_REQUEST:
				handleTraceStartRequest((Bundle)msg.obj);
				break;
			case MSG_TRACE_START_RESULT:
				handleTraceStartResult((Bundle)msg.obj);
				break;
			case MSG_TRACE_STOP_REQUEST:
				handleTraceStopRequest((Bundle)msg.obj);
				break;
			case MSG_TRACE_STOP_RESULT:
				handleTraceStopResult((Bundle)msg.obj);
				break;
			case MSG_TRACE_CLEAR_REQUEST:
				handleTraceClearRequest((Bundle)msg.obj);
				break;
			case MSG_TRACE_CLEAR_RESULT:
				handleTraceClearResult((Bundle)msg.obj);
				break;
			case MSG_TRACE_GENERAL_REQUEST:
				handleTraceGeneralRequest((Bundle)msg.obj);
				break;
			case MSG_TRACE_GENERAL_RESULT:
				handleTraceGeneralResult((Bundle)msg.obj);
				break;
			default:
				break;
			}
		}
	}

	private void handleTraceStartRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceStartRequest");
		synchronized (mLock) {
			for(ProcessTraceObserver observer : mSetObserver) {
				observer.onTraceStartRequest(more);
			}
		}
	}

	private void handleTraceStartResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceStartResult");
		sendResultBroadcast(Constants.ACTION_PROCESS_START_RESULT, more);
    }

	private void handleTraceStopRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceStopRequest");
		synchronized (mLock) {
			for(ProcessTraceObserver observer : mSetObserver) {
				observer.onTraceStopRequest(more);
			}
		}
	}

	private void handleTraceStopResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceStopResult");
		sendResultBroadcast(Constants.ACTION_PROCESS_STOP_RESULT, more);
    }

	private void handleTraceClearRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceClearRequest");
		synchronized (mLock) {
			for(ProcessTraceObserver observer : mSetObserver) {
				observer.onTraceClearRequest(more);
			}
		}
	}

	private void handleTraceClearResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceClearResult");
		sendResultBroadcast(Constants.ACTION_PROCESS_CLEAR_RESULT, more);
	}

	private void handleTraceGeneralRequest(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceGeneralRequest");
		synchronized (mLock) {
			for(ProcessTraceObserver observer : mSetObserver) {
				observer.onTraceGeneralRequest(more);
			}
		}
	}

	private void handleTraceGeneralResult(Bundle more) {
		LogHelper.v("ProcessTraceClient.handleTraceGeneralResult");
		sendResultBroadcast(Constants.ACTION_PROCESS_GENERAL_RESULT, more);
	}

	private void sendResultBroadcast(String action, Bundle more) {
		Intent intent = new Intent().setPackage(Constants.PKG_SERVER);
		intent.setAction(action);
		intent.putExtra(Constants.EXTRA_UID, mContext.getApplicationInfo().uid);
		int pid = android.os.Process.myPid();
		intent.putExtra(Constants.EXTRA_PID, pid);
		if (more != null) intent.putExtra(Constants.EXTRA_MORE, more);
		mContext.sendBroadcast(intent);
	}
}
