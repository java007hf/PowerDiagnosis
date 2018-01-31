package com.tencent.powerhook;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.location.LocationListener;
import android.os.SystemClock;

import com.tencent.qrom.powerdiagnosis.common.ProcessStats;
import com.tencent.qrom.powerdiagnosis.common.ProcessStatsImpl;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

public class ProcessStatsHelper {
	private static ProcessStats sProcessStats;
	private static ProcessStatsHelper sProcessStatsHelper;
	private static boolean sCanNoteFlag = false;
	private static Object LOCK = new Object();
	
	private ProcessStatsHelper() {
		sProcessStats = new ProcessStatsImpl();
	}
	
	public static ProcessStatsHelper getInstance() {
		if (sProcessStatsHelper == null) {
			sProcessStatsHelper = new ProcessStatsHelper();
		}
		return sProcessStatsHelper;
	}
	
	public void setPackageName(String packageName) {
		synchronized (LOCK) {
			sProcessStats.setPackageName(packageName);
		}
	}
	
    public void setProcessName(String ProcessName) {
    	synchronized (LOCK) {
    		sProcessStats.setProcessName(ProcessName);
    	}
    }
    
    public String getProcessName() {
    	return sProcessStats.getProcessName();
    }
    
    public void setUid(int uid) {
    	synchronized (LOCK) {
    		sProcessStats.setUid(uid);
    	}
    }
    
    public void setPid(int pid) {
    	synchronized (LOCK) {
    		sProcessStats.setPid(pid);
    	}
    }
    
    public int getPid() {
    	return sProcessStats.getPid();
    }
    
    public long save(String path) {
		synchronized (LOCK) {
			long t = SystemClock.uptimeMillis();
			setCanNoteFlag(false);
			sProcessStats.save(path, t);
			sProcessStats.clear();
	    	
	    	return t;
		}
    }
    
    public void dump() {
    	synchronized (LOCK) {
    		sProcessStats.dump();
		}
    }
    
    public void clear() {
    	synchronized (LOCK) {
    		setCanNoteFlag(false);
    		sProcessStats.clear();
    		setCanNoteFlag(true);
    	}
    }
    
    public String[] getFileList(Context context) {
    	File cacheFile = context.getCacheDir();
    	File[] listFiles = cacheFile.listFiles();
    	
    	List<String> list = new ArrayList<String>();
    	if (listFiles == null) {
    		return new String[0];
    	}
    	
    	for(File file : listFiles) {
    		String name = file.getName();
    		if (name.startsWith("process_info")) {
    			list.add(file.getAbsolutePath());
    		}
    	}
    	
    	String[] fileStrings = new String[list.size()];
    	for(int i=0;i<list.size();i++) {
    		fileStrings[i] = list.get(i);
    	}
    	return fileStrings;
    }
    
    public void clearFile(Context context) {
    	File cacheFile = context.getCacheDir();
    	File[] listFiles = cacheFile.listFiles();
    	
    	if (listFiles == null) return;
    	
    	for(File file : listFiles) {
    		String name = file.getName();
    		if (name.startsWith("process_info")) {
    			file.delete();
    		}
    	}
    }
    
    public void setCanNoteFlag(boolean flag) {
    	sCanNoteFlag = flag;
    }
    
	public void noteStartMethod(MethodHookParam param, Throwable throwable){
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String callString = param.method.getDeclaringClass().getName() + "."
					+ param.method.getName();
			LogHelper.d("noteStartMethod = " + callString);
			int hashcode = param.hashCode();
			sProcessStats.noteStartMethod(callString, throwable, hashcode);
		}
	}
	
	public void noteStopMethod(MethodHookParam param){
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			LogHelper.d("========noteStopMethod=========");
			int hashcode = param.hashCode();
			sProcessStats.noteStopMethod(hashcode);
		}
	}

	public void noteStartWakelock(String tag, Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			sProcessStats.noteStartWakelock(tag, throwable);
		}
	}
	
	public void noteStopWakelock(String tag) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			sProcessStats.noteStopWakelock(tag);
		}
	}
	
	public void noteRequestLocationUpdates(LocationListener listener, Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = listener.hashCode() + ":LocationListener:" + listener.getClass().getName();
			sProcessStats.noteRequestLocationUpdates(className, throwable);
		}
	}
	
	public void noteRequestLocationUpdates(PendingIntent intent, Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = intent.hashCode() + ":PendingIntent:" + intent.getClass().getName();
			sProcessStats.noteRequestLocationUpdates(className, throwable);
		}
	}
	
	public void noteRemoveUpdates(LocationListener listener) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = listener.hashCode() + ":LocationListener:" + listener.getClass().getName();
			sProcessStats.noteRemoveUpdates(className);
		}
	}
	
	public void noteRemoveUpdates(PendingIntent intent) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = intent.hashCode() + ":PendingIntent:" + intent.getClass().getName();
			sProcessStats.noteRemoveUpdates(className);
		}
	}
	
	public void noteStartWifiScan(Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			sProcessStats.noteStartWifiScan(throwable);
		}
	}
	
	public void noteStartMediaScan(Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			sProcessStats.noteStartMediaScan(throwable);
		}
	}
	
	public void noteRegisterLegacyListener(SensorListener listener,
			int sensors, Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = listener.hashCode() + ":SensorListener:" + listener.getClass().getName();
			sProcessStats.noteRegisterSensorListener(className, sensors, throwable);
		}
	}
	
	public void noteRegisterLegacyListener(SensorEventListener listener,
			int sensors, Throwable throwable) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = listener.hashCode() + ":SensorEventListener:" + listener.getClass().getName();
			sProcessStats.noteRegisterSensorListener(className, sensors, throwable);
		}
	}

	public void noteUnregisterLegacyListener(SensorListener listener,
			int sensors) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = listener.hashCode() + ":SensorListener:" + listener.getClass().getName();
			sProcessStats.noteUnregisterSensorListener(className, sensors);
		}
	}
	
	public void noteUnregisterLegacyListener(SensorEventListener listener,
			int sensors) {
		if (!sCanNoteFlag) return;
		synchronized (LOCK) {
			String className = listener.hashCode() + ":SensorEventListener:" + listener.getClass().getName();
			sProcessStats.noteUnregisterSensorListener(className, sensors);
		}
	}
}
