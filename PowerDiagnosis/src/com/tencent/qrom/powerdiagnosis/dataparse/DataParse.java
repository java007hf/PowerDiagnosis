package com.tencent.qrom.powerdiagnosis.dataparse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.util.ArrayMap;

import com.tencent.qrom.powerdiagnosis.common.ProcessStats;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Gps;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.MediaScan;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.MethodStats;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Sensor;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Stats;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Wakelock;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats.WifiScan;

public class DataParse {
	private static DataParse mDataParse;
	public static final String CATEGORY_CPU = "CPU";
	public static final String CATEGORY_SENSOR = "SENSOR";
	public static final String CATEGORY_WIFISCAN = "WIFISCAN";
	public static final String CATEGORY_GPS = "GPS";
	public static final String CATEGORY_WAKELOCK = "WAKELOCK";
	public static final String CATEGORY_MEDIASCAN = "MEDIASCAN";
	
	public static final int DATA_NONE = -1;

	private DataParse() {

	}

	public static DataParse getInstance() {
		if (mDataParse == null) {
			mDataParse = new DataParse();
		}

		return mDataParse;
	}

	@SuppressLint("NewApi")
	public DataCategory getCpuData(
			ArrayMap<String, HashMap<String, MethodStats>> cpu) {
		DataCategory cpuData = new DataCategory(CATEGORY_CPU);
		Iterator<Entry<String, HashMap<String, MethodStats>>> cpuIterator = cpu
				.entrySet().iterator();
		while (cpuIterator.hasNext()) {
			Entry<String, HashMap<String, MethodStats>> traceEntry = cpuIterator
					.next();

			HashMap<String, MethodStats> mrHashMap = traceEntry.getValue();
			Iterator<Entry<String, MethodStats>> traceIterator2 = mrHashMap
					.entrySet().iterator();

			StatsData threadCpuData = new StatsData(CATEGORY_CPU);
			cpuData.statsDatas.add(threadCpuData);
			threadCpuData.name = traceEntry.getKey();

			while (traceIterator2.hasNext()) {
				Entry<String, MethodStats> methodRunEntry = traceIterator2
						.next();
				Stats methodStats = methodRunEntry.getValue();
				StatsInfo statsInfo = new StatsInfo(methodStats, methodRunEntry.getKey());
				threadCpuData.list.add(statsInfo);

				threadCpuData.totalTime += methodStats.getInfo().getTotalTime();
				threadCpuData.totalCount += methodStats.getInfo().getTotalCount();
			}

			StatsComparator vc = new StatsComparator();
			Collections.sort(threadCpuData.list, vc);

			cpuData.totalTime += threadCpuData.totalTime;
			cpuData.totalCount += threadCpuData.totalCount;
		}
		
		Collections.sort(cpuData.statsDatas);

		return cpuData;
	}

	@SuppressLint("NewApi")
	public DataCategory getSensorData(
			ArrayMap<Integer, HashMap<String, Sensor>> sensor) {
		DataCategory sensorData = new DataCategory(CATEGORY_SENSOR);
		Iterator<Entry<Integer, HashMap<String, Sensor>>> sensorIterator = sensor
				.entrySet().iterator();
		while (sensorIterator.hasNext()) {
			Entry<Integer, HashMap<String, Sensor>> traceEntry = sensorIterator
					.next();

			HashMap<String, Sensor> mrHashMap = traceEntry.getValue();
			Iterator<Entry<String, Sensor>> traceIterator2 = mrHashMap
					.entrySet().iterator();

			StatsData typeSensorData = new StatsData(CATEGORY_SENSOR);
			sensorData.statsDatas.add(typeSensorData);
			typeSensorData.name = "sensorid:" + traceEntry.getKey();

			while (traceIterator2.hasNext()) {
				Entry<String, Sensor> methodRunEntry = traceIterator2.next();
				Stats methodStats = methodRunEntry.getValue();
				StatsInfo statsInfo = new StatsInfo(methodStats, methodRunEntry.getKey());
				typeSensorData.list.add(statsInfo);

				typeSensorData.totalTime += methodStats.getInfo()
						.getTotalTime();
				typeSensorData.totalCount += methodStats.getInfo().getTotalCount();
			}

			StatsComparator vc = new StatsComparator();
			Collections.sort(typeSensorData.list, vc);

			sensorData.totalTime += typeSensorData.totalTime;
			sensorData.totalCount += typeSensorData.totalCount;
		}
		
		Collections.sort(sensorData.statsDatas);
		
		return sensorData;
	}

	@SuppressLint("NewApi")
	public DataCategory getWakelockData(ArrayMap<String, Wakelock> wakelock) {
		DataCategory wakelockData = new DataCategory(CATEGORY_WAKELOCK);

		StatsData typeWakeData = new StatsData(CATEGORY_WAKELOCK);
		wakelockData.statsDatas.add(typeWakeData);

		Iterator<Entry<String, Wakelock>> traceIterator2 = wakelock.entrySet()
				.iterator();
		while (traceIterator2.hasNext()) {
			Entry<String, Wakelock> methodRunEntry = traceIterator2.next();
			Stats methodStats = methodRunEntry.getValue();
			StatsInfo statsInfo = new StatsInfo(methodStats, methodRunEntry.getKey());
			typeWakeData.list.add(statsInfo);
			typeWakeData.totalCount += methodStats.getInfo().getTotalCount();
			typeWakeData.totalTime += methodStats.getInfo().getTotalTime();
		}

		StatsComparator vc = new StatsComparator();
		Collections.sort(typeWakeData.list, vc);

		wakelockData.totalTime = typeWakeData.totalTime;
		wakelockData.totalCount = typeWakeData.totalCount;

		return wakelockData;
	}
	
	@SuppressLint("NewApi")
	public DataCategory getGpsData(ArrayMap<String, Gps> gps) {
		DataCategory gpsData = new DataCategory(CATEGORY_GPS);

		StatsData typeGpsData = new StatsData(CATEGORY_GPS);
		gpsData.statsDatas.add(typeGpsData);

		Iterator<Entry<String, Gps>> traceIterator2 = gps.entrySet()
				.iterator();
		while (traceIterator2.hasNext()) {
			Entry<String, Gps> methodRunEntry = traceIterator2.next();
			Stats methodStats = methodRunEntry.getValue();
			StatsInfo statsInfo = new StatsInfo(methodStats, methodRunEntry.getKey());
			typeGpsData.list.add(statsInfo);

			typeGpsData.totalTime += methodStats.getInfo().getTotalTime();
			typeGpsData.totalCount += methodStats.getInfo().getTotalCount();
		}

		StatsComparator vc = new StatsComparator();
		Collections.sort(typeGpsData.list, vc);

		gpsData.totalTime = typeGpsData.totalTime;
		gpsData.totalCount = typeGpsData.totalCount;

		return gpsData;
	}
	
	@SuppressLint("NewApi")
	public DataCategory getWifiScanData(WifiScan wifiscan) {
		
		DataCategory wifiscanData = new DataCategory(CATEGORY_WIFISCAN);

		StatsData typeWifiscanData = new StatsData(CATEGORY_WIFISCAN);
		wifiscanData.statsDatas.add(typeWifiscanData);
		StatsInfo statsInfo = new StatsInfo(wifiscan, CATEGORY_WIFISCAN);
		typeWifiscanData.list.add(statsInfo);
		typeWifiscanData.totalTime = wifiscan.getInfo().getTotalTime();
		typeWifiscanData.totalCount = wifiscan.getInfo().getTotalCount();

		wifiscanData.totalTime = DATA_NONE;
		wifiscanData.totalCount = typeWifiscanData.totalCount;

		return wifiscanData;
	}
	
	@SuppressLint("NewApi")
	public DataCategory getMediaScanData(MediaScan mediascan) {
		
		DataCategory mediascanData = new DataCategory(CATEGORY_MEDIASCAN);

		StatsData typeMediascanData = new StatsData(CATEGORY_MEDIASCAN);
		mediascanData.statsDatas.add(typeMediascanData);
		StatsInfo statsInfo = new StatsInfo(mediascan, CATEGORY_MEDIASCAN);
		typeMediascanData.list.add(statsInfo);
		typeMediascanData.totalTime = mediascan.getInfo().getTotalTime();
		typeMediascanData.totalCount = mediascan.getInfo().getTotalCount();

		mediascanData.totalTime = DATA_NONE;
		mediascanData.totalCount = typeMediascanData.totalCount;

		return mediascanData;
	}
	
	public List<DataCategory> getDataCategories(ProcessStats processStats) {
		DataCategory cpuCategory = getCpuData(processStats.getCpuStats());
		DataCategory sensorCategory = getSensorData(processStats.getSensorStats());
		DataCategory wakelockCategory = getWakelockData(processStats.getWakelockStats());
		DataCategory gpsCategory = getGpsData(processStats.getGpsStats());
		DataCategory wifiscanCategory = getWifiScanData(processStats.getWifiScanStats());
		DataCategory mediascanCategory = getMediaScanData(processStats.getMediaScanStats());
		
		List<DataCategory> dataCategories = new ArrayList<DataCategory>();
		dataCategories.add(cpuCategory);
		dataCategories.add(sensorCategory);
		dataCategories.add(wakelockCategory);
		dataCategories.add(gpsCategory);
		dataCategories.add(wifiscanCategory);
		dataCategories.add(mediascanCategory);
		
		return dataCategories;
	}
}
