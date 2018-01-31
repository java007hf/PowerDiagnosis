package com.tencent.qrom.powerdiagnosis.common;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.Log;

import com.tencent.powerhook.Utils;
import com.tencent.powerhook.hookfun.SystemSensorManagerHook;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

@SuppressLint("NewApi")
public class ProcessStatsImpl extends ProcessStats {
	private static final String TAG = "ProcessStats";
	private long mStartRecordTime = 0;
	private String mPackageName;
	private String mProcessName;
	private int mUid;
	private int mPid;
	
	private ArrayMap<String, HashMap<String,MethodStats>> mCpuData = new ArrayMap<String, HashMap<String, MethodStats>>(); // thread-methods-method
	private ArrayMap<Integer, HashMap<String, Sensor>> mSensorData = new ArrayMap<Integer, HashMap<String, Sensor>>(); // type-listerClass-Sensor
	private ArrayMap<String, Wakelock> mWakelockData = new ArrayMap<String, Wakelock>(); // tag-wakelock
	private ArrayMap<String, Gps> mGpsData = new ArrayMap<String, Gps>(); //listerClass-GpsItem
	private WifiScanImpl mWifiScanData = new WifiScanImpl();
	private MediaScanImpl mMediaScanData = new MediaScanImpl();

	public ProcessStatsImpl() {
		mStartRecordTime = SystemClock.elapsedRealtime();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeLong(mStartRecordTime);
		dest.writeString(mPackageName);
		dest.writeString(mProcessName);
		dest.writeInt(mUid);
		dest.writeInt(mPid);
		
		//cpu data
		int arraySize = mCpuData.size();
		int hashSize = 0;
		dest.writeInt(arraySize); //
		for(Entry<String, HashMap<String, MethodStats>> ents : mCpuData.entrySet()) {
			dest.writeString(ents.getKey()); //
			HashMap<String, MethodStats> hashMap = ents.getValue();
			hashSize = hashMap.size();
			dest.writeInt(hashSize); //
			
			for(Entry<String, MethodStats> entry : hashMap.entrySet()) {
				dest.writeString(entry.getKey()); //
				entry.getValue().writeToParcel(dest, flag); //
			}
		}
		
		//sensor data
		arraySize = mSensorData.size();
		dest.writeInt(arraySize); //
		for(Entry<Integer, HashMap<String, Sensor>> ents : mSensorData.entrySet()) {
			dest.writeInt(ents.getKey()); //
			HashMap<String, Sensor> hashMap = ents.getValue();
			hashSize = hashMap.size();
			dest.writeInt(hashSize); //
			
			for(Entry<String, Sensor> entry : hashMap.entrySet()) {
				dest.writeString(entry.getKey()); //
				entry.getValue().writeToParcel(dest, flag); //
			}
		}
		
		//wakelock data
		hashSize = mWakelockData.size();
		dest.writeInt(hashSize); //
		
		for(Entry<String, Wakelock> entry : mWakelockData.entrySet()) {
			dest.writeString(entry.getKey()); //
			entry.getValue().writeToParcel(dest, flag); //
		}
		
		//gps data
		hashSize = mGpsData.size();
		dest.writeInt(hashSize); //
		
		for(Entry<String, Gps> entry : mGpsData.entrySet()) {
			dest.writeString(entry.getKey()); //
			entry.getValue().writeToParcel(dest, flag); //
		}
		
		//wifiscan data
		mWifiScanData.writeToParcel(dest, flag);
		
		//mediascan data
		mMediaScanData.writeToParcel(dest, flag);
	}

	public ProcessStatsImpl(Parcel in) {
		mStartRecordTime = in.readLong();
		mPackageName = in.readString();
		mProcessName = in.readString();
		mUid = in.readInt();
		mPid = in.readInt();
		
		//cpu data
		int arraySize = in.readInt();
		int hashSize = 0;
		for(int i=0;i<arraySize;i++) {
			String threadName = in.readString();
			hashSize = in.readInt();
			HashMap<String, MethodStats> hashMap = new HashMap<String, MethodStats>();
			mCpuData.put(threadName, hashMap);
			for(int j=0;j<hashSize;j++) {
				String methodName = in.readString();
				MethodStatsImpl m = new MethodStatsImpl(in);
				hashMap.put(methodName, m);
			}
		}
		
		//sensor data
		arraySize = in.readInt();
		for(int i=0;i<arraySize;i++) {
			int type = in.readInt();
			hashSize = in.readInt();
			HashMap<String, Sensor> hashMap = new HashMap<String, Sensor>();
			mSensorData.put(type, hashMap);
			for(int j=0;j<hashSize;j++) {
				String className = in.readString();
				Sensor s = new SensorImpl(in);
				hashMap.put(className, s);
			}
		}
		
		//wakelock data
		hashSize = in.readInt();
		for(int j=0;j<hashSize;j++) {
			String tag = in.readString();
			Wakelock s = new WakelockImpl(in);
			mWakelockData.put(tag, s);
		}
		
		//gps data
		hashSize = in.readInt();
		for(int j=0;j<hashSize;j++) {
			String tag = in.readString();
			Gps s = new GpsImpl(in);
			mGpsData.put(tag, s);
		}
		
		//wifiscan data
		mWifiScanData = new WifiScanImpl(in);
		
		//mediascan data
		mMediaScanData = new MediaScanImpl(in);
	}
	public void setPackageName(String packageName) {
		mPackageName = packageName;
	}

	public void setProcessName(String ProcessName) {
		mProcessName = ProcessName;
	}

	public void setUid(int uid) {
		mUid = uid;
	}

	public void setPid(int pid) {
		mPid = pid;
	}

	@Override
	public String getPackageName() {
		return mPackageName;
	}

	@Override
	public String getProcessName() {
		return mProcessName;
	}

	@Override
	public int getUid() {
		return mUid;
	}

	@Override
	public int getPid() {
		return mPid;
	}

	@Override
	public long getStartRecordTime() {
		return mStartRecordTime;
	}

	@Override
	public ArrayMap<String, Wakelock> getWakelockStats() {
		return mWakelockData;
	}

	@Override
	public WifiScan getWifiScanStats() {
		return mWifiScanData;
	}
	
	@Override
	public MediaScan getMediaScanStats() {
		return mMediaScanData;
	}

	@Override
	public ArrayMap<String, Gps> getGpsStats() {
		return mGpsData;
	}

	@Override
	public ArrayMap<Integer, HashMap<String, Sensor>> getSensorStats() {
		return mSensorData;
	}
	
	@Override
	public void dump() {
		LogHelper.d("//////////////////////////////////////////////////////////");
		LogHelper.d("//           PackageName : " + mPackageName);
		LogHelper.d("//           ProcessName : " + mProcessName);
		LogHelper.d("//           Uid : " + mUid);
		LogHelper.d("//           Pid : " + mPid);
		LogHelper.d("//////////////////////////////////////////////////////////\n");
		
		LogHelper.d("----------------------------------------------------------");
		LogHelper.d("DUMP OF CPU:");
		Iterator<Entry<String, HashMap<String, MethodStats>>>  cpuIterator = mCpuData.entrySet().iterator();
		while (cpuIterator.hasNext()) {
			Entry<String, HashMap<String, MethodStats>> traceEntry = cpuIterator.next();
			
			String threadName = traceEntry.getKey();
			HashMap<String, MethodStats> mrHashMap = traceEntry.getValue();
			
			LogHelper.d("####################Thread Name : " + threadName + "########################");
			Iterator<Entry<String, MethodStats>> traceIterator2 
				= mrHashMap.entrySet().iterator();
			
			while(traceIterator2.hasNext()) {
				Entry<String, MethodStats> methodRunEntry = traceIterator2.next();
				
				String MethodName = methodRunEntry.getKey();
				MethodStats methodStats = methodRunEntry.getValue();
				
				LogHelper.d("=================Method Name : " + MethodName + "=================");
				LogHelper.d("=================Method TotalTime : " + methodStats.getInfo().getTotalTime() + "=================");
				LogHelper.d("=================Method TotalCount : " + methodStats.getInfo().getTotalCount() + "=================");
				
				for(Item item : methodStats.getInfo().mItems) {
					List<String> tList = item.mStackTraces;
					for(String string : tList) {
						LogHelper.d(string);
					}
				}
			}
		}
		
		LogHelper.d("----------------------------------------------------------");
		LogHelper.d("DUMP OF GPS:");
		Iterator<Entry<String, Gps>> gspIterator = mGpsData.entrySet().iterator();
		while (gspIterator.hasNext()) {
			Entry<String, Gps> entry = gspIterator
					.next();
			String name = entry.getKey();
			Gps gps = entry.getValue();
			
			LogHelper.d("=================GPS Name : " + name + "=================");
			LogHelper.d("=================GPS TotalTime : " + gps.getInfo().getTotalTime() + "=================");
			LogHelper.d("=================GPS TotalCount : " + gps.getInfo().getTotalCount() + "=================");
			
			for(Item item : gps.getInfo().mItems) {
				List<String> tList = item.mStackTraces;
				for(String string : tList) {
					Log.d(TAG, string);
				}
			}
		}

		LogHelper.d("----------------------------------------------------------");
		LogHelper.d("DUMP OF WAKELOCK:");
		Iterator<Entry<String, Wakelock>> wakelockIterator = mWakelockData.entrySet().iterator();
		while (wakelockIterator.hasNext()) {
			Entry<String, Wakelock> entry = wakelockIterator
					.next();
			
			String tag = entry.getKey();
			Wakelock wakelock = entry.getValue();
			
			LogHelper.d("=================WAKELOCK TAG : " + tag + "=================");
			LogHelper.d("=================wakelock TotalTime : " + wakelock.getInfo().getTotalTime() + "=================");
			LogHelper.d("=================wakelock TotalCount : " + wakelock.getInfo().getTotalCount() + "=================");
			
			for(Item item : wakelock.getInfo().mItems) {
				List<String> tList = item.mStackTraces;
				for(String string : tList) {
					Log.d(TAG, string);
				}
			}
		}

		LogHelper.d("----------------------------------------------------------");
		LogHelper.d("DUMP OF SENSOR:");
		Iterator<Entry<Integer, HashMap<String, Sensor>>>  senIterator = mSensorData.entrySet().iterator();
		while (senIterator.hasNext()) {
			Entry<Integer, HashMap<String, Sensor>> traceEntry = senIterator.next();
			
			Integer type = traceEntry.getKey();
			HashMap<String, Sensor> mrHashMap = traceEntry.getValue();
			
			LogHelper.d("=================Sensor type : " + type + "=================");
			Iterator<Entry<String, Sensor>> traceIterator2 
				= mrHashMap.entrySet().iterator();
			
			while(traceIterator2.hasNext()) {
				Entry<String, Sensor> methodRunEntry = traceIterator2.next();
				
				String className = methodRunEntry.getKey();
				Sensor sensor = methodRunEntry.getValue();
				LogHelper.d("=================SensorListener Name : " + className + "=================");
				LogHelper.d("=================sensor TotalTime : " + sensor.getInfo().getTotalTime() + "=================");
				LogHelper.d("=================sensor TotalCount : " + sensor.getInfo().getTotalCount() + "=================");
				
				for(Item item : sensor.getInfo().mItems) {
					List<String> tList = item.mStackTraces;
					for(String string : tList) {
						Log.d(TAG, string);
					}
				}
			}
		}

		LogHelper.d("----------------------------------------------------------");
		LogHelper.d("DUMP OF WIFISCAN:");
		
		LogHelper.d("=================WIFISCAN TotalTime : " + mWifiScanData.getInfo().getTotalTime() + "=================");
		LogHelper.d("=================WIFISCAN TotalCount : " + mWifiScanData.getInfo().getTotalCount() + "=================");
		
		for(Item item : mWifiScanData.getInfo().mItems) {
			List<String> tList = item.mStackTraces;
			for(String string : tList) {
				Log.d(TAG, string);
			}
		}
		
		LogHelper.d("----------------------------------------------------------");
		LogHelper.d("DUMP OF MEDIASCAN:");
		
		LogHelper.d("=================MEDIASCAN TotalTime : " + mMediaScanData.getInfo().getTotalTime() + "=================");
		LogHelper.d("=================MEDIASCAN TotalCount : " + mMediaScanData.getInfo().getTotalCount() + "=================");
		
		for(Item item : mMediaScanData.getInfo().mItems) {
			List<String> tList = item.mStackTraces;
			for(String string : tList) {
				Log.d(TAG, string);
			}
		}
	}

	@Override
	public void clear() {
		Iterator<Entry<String, HashMap<String, MethodStats>>>  cpuIterator = mCpuData.entrySet().iterator();
		while (cpuIterator.hasNext()) {
			Entry<String, HashMap<String, MethodStats>> traceEntry = cpuIterator.next();
			
			HashMap<String, MethodStats> mrHashMap = traceEntry.getValue();
			Iterator<Entry<String, MethodStats>> traceIterator2 
				= mrHashMap.entrySet().iterator();
			
			while(traceIterator2.hasNext()) {
				Entry<String, MethodStats> methodRunEntry = traceIterator2.next();
				MethodStats methodStats = methodRunEntry.getValue();
				methodStats.clear();
			}
			mrHashMap.clear();
		}
		mCpuData.clear();
		runItemHashMap.clear();

		Iterator<Entry<String, Wakelock>> wakelockIterator = mWakelockData.entrySet().iterator();
		while (wakelockIterator.hasNext()) {
			Entry<String, Wakelock> entry = wakelockIterator
					.next();
			Wakelock wakelock = entry.getValue();
			if (wakelock != null)
				wakelock.clear();
		}
		mWakelockData.clear();

		Iterator<Entry<Integer, HashMap<String, Sensor>>> sensorIterator 
			= mSensorData.entrySet().iterator();
		while (sensorIterator.hasNext()) {
			Entry<Integer,HashMap<String, Sensor>> entry = sensorIterator
					.next();
			HashMap<String, Sensor> sensorHashMap = entry.getValue();


			Iterator<Entry<String, Sensor>> traceIterator2 
				= sensorHashMap.entrySet().iterator();
			
			while(traceIterator2.hasNext()) {
				Entry<String, Sensor> methodRunEntry = traceIterator2.next();
				Sensor sensor = methodRunEntry.getValue();
				sensor.clear();
			}
			sensorHashMap.clear();
		
		}
		mSensorData.clear();

		mWifiScanData.clear();
		
		mMediaScanData.clear();

		Iterator<Entry<String, Gps>> gpsIterator = mGpsData.entrySet().iterator();
		while (gpsIterator.hasNext()) {
			Entry<String, Gps> entry = gpsIterator
					.next();
			Gps gps = entry.getValue();
			if (gps != null)
				gps.clear();
		}
		mGpsData.clear();

		mStartRecordTime = SystemClock.elapsedRealtime();
	}

	@Override
	public void save(String path, long endTime) {
		LogHelper.d("SAVE CPU:");
		//compute cpu
		Iterator<Entry<String, HashMap<String, MethodStats>>>  cpuIterator = mCpuData.entrySet().iterator();
		while (cpuIterator.hasNext()) {
			Entry<String, HashMap<String, MethodStats>> traceEntry = cpuIterator.next();
			HashMap<String, MethodStats> mrHashMap = traceEntry.getValue();
			Iterator<Entry<String, MethodStats>> traceIterator2 
				= mrHashMap.entrySet().iterator();
			
			while(traceIterator2.hasNext()) {
				Entry<String, MethodStats> methodRunEntry = traceIterator2.next();
				MethodStats methodStats = methodRunEntry.getValue();
				Info info = methodStats.getInfo();
				List<Item> items = info.getItems();
				long max = 0;
				Item maxItem = null;
				long total = 0;
				for(int i=0;i<items.size();i++) {
					Item item = items.get(i);
					
					long used = 0;
					if (item.endTime == 0) {
						used = 0;
					} else {
						used = item.endTime - item.startTime;
					}
					
					LogHelper.d("used : " + used);
					LogHelper.d("max : " + max);
					
					if (used >= max && item.mThrowables != null) {
						info.mMaxItemIndex = i;
						max = used;
						maxItem = item;
					}
					
					LogHelper.d("maxItem : " + maxItem);
					
					total = total + used;
				}
				info.mTotalTime = total;
				
				for(Item item : items) {
					if (item != maxItem) {
						item.mStackTraces.clear();
						if (item.mThrowables != null) {
							item.mThrowables.clear();
							item.mThrowables = null;
						}
					} else {
						String t = Utils.printStack(item.mThrowables.get(0));
						item.mStackTraces.add(t);
					}
				}
			}
		}
		
		LogHelper.d("SAVE sensor:");
		//compute sensor
		Iterator<Entry<Integer, HashMap<String, Sensor>>>  sensorIterator = mSensorData.entrySet().iterator();
		while (sensorIterator.hasNext()) {
			Entry<Integer, HashMap<String, Sensor>> traceEntry = sensorIterator.next();
			HashMap<String, Sensor> mrHashMap = traceEntry.getValue();
			Iterator<Entry<String, Sensor>> traceIterator2 
				= mrHashMap.entrySet().iterator();
			
			while(traceIterator2.hasNext()) {
				Entry<String, Sensor> sensorEntry = traceIterator2.next();
				
				Sensor sensorStats = sensorEntry.getValue();
				Info info = sensorStats.getInfo();
				List<Item> items = info.getItems();
				long total = 0;
				for(Item item : items) {
					List<Throwable> throwables = item.mThrowables;
					item.mStackTraces.clear();
					for(Throwable throwable : throwables) {
						item.mStackTraces.add(Utils.printStack(throwable));
					}
					if (item.endTime == 0) item.endTime = endTime;
					long used = item.endTime - item.startTime;
					total = total + used;
				}
				info.mTotalTime = total;
			}
		}
		
		LogHelper.d("SAVE wakelock:");
		//compute wakelock
		Iterator<Entry<String, Wakelock>>  wakelockIterator = mWakelockData.entrySet().iterator();
		while(wakelockIterator.hasNext()) {
			Entry<String, Wakelock> methodRunEntry = wakelockIterator.next();
			
			Wakelock wakelockStats = methodRunEntry.getValue();
			Info info = wakelockStats.getInfo();
			List<Item> items = info.getItems();
			long total = 0;
			for(Item item : items) {
				List<Throwable> throwables = item.mThrowables;
				item.mStackTraces.clear();
				for(Throwable throwable : throwables) {
					item.mStackTraces.add(Utils.printStack(throwable));
				}
				if (item.endTime == 0) item.endTime = endTime;
				long used = item.endTime - item.startTime;
				total = total + used;
			}
			info.mTotalTime = total;
		}
		
		LogHelper.d("SAVE gps:");
		//compute gps
		Iterator<Entry<String, Gps>> gpsIterator = mGpsData.entrySet().iterator();
		while(gpsIterator.hasNext()) {
			Entry<String, Gps> gpsEntry = gpsIterator.next();
			
			Gps gpsStats = gpsEntry.getValue();
			Info info = gpsStats.getInfo();
			List<Item> items = info.getItems();
			long total = 0;
			for(Item item : items) {
				List<Throwable> throwables = item.mThrowables;
				item.mStackTraces.clear();
				for(Throwable throwable : throwables) {
					String string = Utils.printStack(throwable);
					item.mStackTraces.add(string);
				}
				if (item.endTime == 0) item.endTime = endTime;
				long used = item.endTime - item.startTime;
				total = total + used;
			}
			info.mTotalTime = total;
		}
		
		LogHelper.d("SAVE wifiscan:");
		//compute wifiscan
		Info wifiInfo = mWifiScanData.getInfo();
		List<Item> wifiItems = wifiInfo.getItems();
		for(Item item : wifiItems) {
			List<Throwable> throwables = item.mThrowables;
			item.mStackTraces.clear();
			for(Throwable throwable : throwables) {
				item.mStackTraces.add(Utils.printStack(throwable));
			}
		}
		
		//compute mediascan
		Info mediaInfo = mMediaScanData.getInfo();
		List<Item> mediaItems = mediaInfo.getItems();
		for(Item item : mediaItems) {
			List<Throwable> throwables = item.mThrowables;
			item.mStackTraces.clear();
			for(Throwable throwable : throwables) {
				item.mStackTraces.add(Utils.printStack(throwable));
			}
		}
		
		Parcel parcel = Parcel.obtain();
		this.writeToParcel(parcel, 0);
		byte[] b = parcel.marshall();
		int size = b.length;
		byte[] sizeb = Utils.toByteArray(size);
		LogHelper.d("save path = " + path + ", size = " + size);
		parcel.recycle();
		
		try {
			FileOutputStream outputStream = new FileOutputStream(path);
			outputStream.write(sizeb);
			outputStream.write(b);
			outputStream.close();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		File file = new File(path);
		if (file.exists())file.setReadable(true, false);
	}

	private HashMap<Integer, Item> runItemHashMap = new HashMap<Integer, Item>();

	@Override
	public void noteStartMethod(String callString, Throwable throwable, int hashCode) {
		String threadName = Thread.currentThread().getName();
		HashMap<String, MethodStats> items = (HashMap<String, MethodStats>) mCpuData
				.get(threadName);
		if (items == null) {
			items = new HashMap<String, MethodStats>();
			mCpuData.put(threadName, items);
		}

		MethodStatsImpl methodStatsImpl = (MethodStatsImpl) items.get(callString);
		if (methodStatsImpl == null) {
			methodStatsImpl = new MethodStatsImpl();
			items.put(callString, methodStatsImpl);
		}

		Info methodInfo = methodStatsImpl.getInfo();

		Item item = new Item();
		item.mInfo = methodInfo;
		runItemHashMap.put(hashCode, item);
		item.mThrowables.add(throwable);
		methodInfo.mItems.add(item);
		item.startTime = SystemClock.uptimeMillis();
	}

	@Override
	public void noteStopMethod(int hashCode) {
		Item item = runItemHashMap.remove(hashCode);
		if (item != null) {
			item.endTime = SystemClock.uptimeMillis();
			
			long used = item.endTime - item.startTime;
			if (item.mInfo.mMaxUsed <= used) {
				item.mInfo.mMaxUsed = used;
				
				for(Item item2 : item.mInfo.mItems) {
					if (item2 != item) {
						if (item2.mThrowables != null) {
							item2.mThrowables.clear();
							item2.mThrowables = null;
						}
					}
				}
			} else {
				if (item.mThrowables != null) {
					item.mThrowables.clear();
					item.mThrowables = null;
				}
			}
		}
	}

	@Override
	public void noteStartWakelock(String tag, Throwable throwable) {
		WakelockImpl wakelock = (WakelockImpl) mWakelockData.get(tag);
		if (wakelock == null) {
			wakelock = new WakelockImpl();
			mWakelockData.put(tag, wakelock);
		}

		Info wakelockInfo = wakelock.getInfo();
		int size = wakelockInfo.mItems.size();
		
		Item wakeLockItem = null;
		if (size > 0) {
			wakeLockItem = (Item) wakelockInfo.mItems.get(size-1);
	    	
	    	if (wakeLockItem.isReleased) {
	    		wakeLockItem = new Item();
	    		wakelockInfo.mItems.add(wakeLockItem);
	    	}
		} else {
			wakeLockItem = new Item();
    		wakelockInfo.mItems.add(wakeLockItem);
    	}
    	wakeLockItem.mThrowables.add(throwable);
	}

	@Override
	public void noteStopWakelock(String tag) {
		WakelockImpl wakelock = (WakelockImpl) mWakelockData.get(tag);
		if (wakelock != null) {
			Info wakelockInfo = wakelock.getInfo();
			int size = wakelockInfo.mItems.size();
			
			if (size > 0) {
				Item wakeLockItem = (Item) wakelockInfo.mItems.get(size-1);
		    	if (!wakeLockItem.isReleased) {
		    		wakeLockItem.release();
		    	}
			}
		}
	}

	@Override
	public ArrayMap<String, HashMap<String, MethodStats>> getCpuStats() {
		return mCpuData;
	}

	public void noteRequestLocationUpdates(String name,
			Throwable throwable) {
		GpsImpl gpsImpl = (GpsImpl) mGpsData.get(name);
		if (gpsImpl == null) {
			gpsImpl = new GpsImpl();
			mGpsData.put(name, gpsImpl);
		}

		Info gpsInfo = gpsImpl.getInfo();
		int size = gpsInfo.mItems.size();
		Item gpsItem = null;
		if (size > 0) {
			gpsItem = gpsInfo.mItems.get(size-1);
			if (gpsItem.isReleased) {
				gpsItem = new Item();
				gpsInfo.mItems.add(gpsItem);
			}
		} else {
			gpsItem = new Item();
			gpsInfo.mItems.add(gpsItem);
		}
		
    	gpsItem.mThrowables.add(throwable);
	}

	public void noteRemoveUpdates(String name) {
		GpsImpl gpsImpl = (GpsImpl) mGpsData.get(name);
		if (gpsImpl != null) {
			Info gpsInfo = gpsImpl.getInfo();
			int size = gpsInfo.mItems.size();
			
			if (size > 0) {
				Item gpsItem = gpsInfo.mItems.get(size-1);
		    	
		    	if (!gpsItem.isReleased) {
		    		gpsItem.release();
		    	}
			}
		}
	}

	@Override
	public void noteStartWifiScan(Throwable throwable) {
		Info wifiScanInfo = mWifiScanData.getInfo();
		Item wifiScanItem = new Item();
		wifiScanItem.mThrowables.add(throwable);
		wifiScanInfo.mItems.add(wifiScanItem);
	}
	
	@Override
	public void noteStartMediaScan(Throwable throwable) {
		Info mediaScanInfo = mMediaScanData.getInfo();
		Item mediaScanItem = new Item();
		mediaScanItem.mThrowables.add(throwable);
		mediaScanInfo.mItems.add(mediaScanItem);
	}

	@Override
	public void noteRegisterSensorListener(String listener, int type,
			Throwable throwable) {
		LogHelper.d("noteRegisterSensorListener " + listener + " " + type);
		HashMap<String, Sensor> items = mSensorData
				.get(type);
		if (items == null) {
			items = new HashMap<String, Sensor>();
			mSensorData.put(type, items);
		}

		SensorImpl sensorStatsImpl = (SensorImpl) items.get(listener);
		if (sensorStatsImpl == null) {
			sensorStatsImpl = new SensorImpl();
			items.put(listener, sensorStatsImpl);
		}

		Info sensorInfo = sensorStatsImpl.getInfo();

		int size = sensorInfo.mItems.size();
		
		Item sensorItem = null;
		if (size > 0) {
			sensorItem = sensorInfo.mItems.get(size-1);
	    	
	    	if (sensorItem.isReleased) {
	    		sensorItem = new Item();
	    		sensorInfo.mItems.add(sensorItem);
	    	}
		} else {
			sensorItem = new Item();
    		sensorInfo.mItems.add(sensorItem);
    	}
    	sensorItem.mThrowables.add(throwable);
	}

	@Override
	public void noteUnregisterSensorListener(String listener, int type) {
		LogHelper.d("noteUnregisterSensorListener " + listener + " " + type);
		if (type == SystemSensorManagerHook.TOTAL_SENSOR_TYPE) {
			Iterator<Entry<Integer, HashMap<String, Sensor>>>  sensorIterator = mSensorData.entrySet().iterator();
			while (sensorIterator.hasNext()) {
				Entry<Integer, HashMap<String, Sensor>> traceEntry = sensorIterator.next();
				HashMap<String, Sensor> hashMap = traceEntry.getValue();
				if (hashMap != null) {
					SensorImpl sensorImpl = (SensorImpl) hashMap.get(listener);
					Info sensorInfo = sensorImpl.getInfo();
					int size = sensorInfo.mItems.size();
					
					if (size > 0) {
						Item sensorItem = sensorInfo.mItems.get(size-1);
						if (!sensorItem.isReleased) {
							sensorItem.release();
						}
					}
				}
			}
			
		} else {
			HashMap<String, Sensor> items = mSensorData
					.get(type);
			if (items != null) {
				SensorImpl sensorImpl = (SensorImpl) items.get(listener);
				Info sensorInfo = sensorImpl.getInfo();
				int size = sensorInfo.mItems.size();
				
				if (size > 0) {
					Item sensorItem = sensorInfo.mItems.get(size-1);
					if (!sensorItem.isReleased) {
						sensorItem.release();
					}
				}
			}
		}
	}
}
