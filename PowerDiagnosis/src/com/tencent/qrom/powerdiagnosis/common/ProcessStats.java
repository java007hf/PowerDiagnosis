package com.tencent.qrom.powerdiagnosis.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.ArrayMap;

public abstract class ProcessStats implements Parcelable {
	//package info
	public abstract String getPackageName();
    public abstract String getProcessName();
    public abstract int getUid();
    public abstract int getPid();
    
    public abstract void setPackageName(String packageName);
    public abstract void setProcessName(String packageName);
    public abstract void setUid(int uid);
    public abstract void setPid(int pid);
    
    //launch info
    public abstract long getStartRecordTime();
    //battery drain info
    public abstract ArrayMap<String, HashMap<String, MethodStats>>  getCpuStats(); // thread - cpu
    public abstract ArrayMap<String, Wakelock> getWakelockStats(); //tag - wakelock
    public abstract WifiScan getWifiScanStats();
    public abstract MediaScan getMediaScanStats();
    public abstract ArrayMap<String, Gps> getGpsStats();
    public abstract ArrayMap<Integer, HashMap<String, Sensor>> getSensorStats(); // id -listerclass-Sensor

    public abstract void save(String path, long endTime);
    public abstract void dump();
    public abstract void clear();
    
    public static class Item implements Parcelable {
    	public Info mInfo;
    	public List<String> mStackTraces = new ArrayList<String>();
    	public List<Throwable> mThrowables = new ArrayList<Throwable>();
    	public boolean isReleased = true;
    	public long startTime = 0;
    	public long endTime = 0;
		
    	public Item() {
    		startTime = SystemClock.uptimeMillis();
			isReleased = false;
		}
		
		public Item(Parcel in) {
			int count = in.readInt();
			for(int i=0;i<count;i++) {
				mStackTraces.add(in.readString());
			}
			
			isReleased = in.readInt()==1;
			startTime = in.readLong();
			endTime = in.readLong();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			int count = mStackTraces.size();
			dest.writeInt(count);
			for(int i=0;i<count;i++) {
				dest.writeString(mStackTraces.get(i));
			}
			
			dest.writeInt(isReleased?1:0);
			dest.writeLong(startTime);
			dest.writeLong(endTime);
		}
		
		public int describeContents() {
			return 0;
		}
		
		public long getStartIime() {
			return startTime;
		}

		public long getStopTime() {
			return endTime;
		}
		
		public void release() {
			endTime = SystemClock.uptimeMillis();
			isReleased = true;
		}
		
		public static final Parcelable.Creator<Item> CREATOR =
	        new Parcelable.Creator<Item>() {
	        @Override
	        public Item createFromParcel(Parcel in) {
	            return new Item(in);
	        }

	        @Override
	        public Item[] newArray(int size) {
	            return new Item[size];
	        }
	    };
	    
        public List<String> getStackTrace() {
			return mStackTraces;
		}
	}

    public static class Info implements Parcelable {
    	public long mTotalTime = 0;
    	public int mMaxItemIndex = 0;
    	public long mMaxUsed = 0;
    	public List<Item> mItems = new ArrayList<Item>();
    	
        public long getTotalTime() {
        	return mTotalTime;
        }
        
		public long getTotalCount() {
			return mItems.size();
		}
		
		public List<Item> getItems() {
			return mItems;
		}
		
		public Info() {
			
		}
		
		public Info(Parcel in) {
			int count = in.readInt();
			for(int i=0;i<count;i++) {
				Item item = new Item(in);
				mItems.add(item);
			}
			mTotalTime = in.readLong();
			mMaxItemIndex = in.readInt();
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			int count = mItems.size();
			dest.writeInt(count);
			for(int i=0;i<count;i++) {
				mItems.get(i).writeToParcel(dest, flags);
			}
			dest.writeLong(mTotalTime);
			dest.writeInt(mMaxItemIndex);
		}
		
		public int describeContents() {
			return 0;
		}
		
		public static final Parcelable.Creator<Info> CREATOR =
	        new Parcelable.Creator<Info>() {
	        @Override
	        public Info createFromParcel(Parcel in) {
	            return new Info(in);
	        }

	        @Override
	        public Info[] newArray(int size) {
	            return new Info[size];
	        }
	    };
	}

    public static abstract class Stats implements Parcelable {
    	Info mInfo = new Info();
    	
        public Stats() {
        }
        
        public Stats(Parcel in) {
        	mInfo = new Info(in);
        }
        
        public void clear() {
    		mInfo.mItems.clear();
    	}
        
        public Info getInfo() {
        	return mInfo;
        }
        
        public void writeToParcel(Parcel dest, int flags) {
        	mInfo.writeToParcel(dest, flags);
    	}
        
        public int describeContents() {
    		return 0;
    	}
    }

    //cpu
    public static abstract class MethodStats extends Stats {
    	public MethodStats() {
		}
    	
		public MethodStats(Parcel in) {
			super(in);
		}
    	
    }
    
    //wakelock
    public static abstract class Wakelock extends Stats {
    	public Wakelock() {
		}
    	
		public Wakelock(Parcel in) {
			super(in);
		}

    }

    //wifi scan
    public static abstract class WifiScan extends Stats {
    	public WifiScan() {
		}
    	
		public WifiScan(Parcel in) {
			super(in);
		}

    }
    
    //media scan
    public static abstract class MediaScan extends Stats {
    	public MediaScan() {
		}
    	
		public MediaScan(Parcel in) {
			super(in);
		}

    }

    //gps
    public static abstract class Gps extends Stats {
    	public Gps() {
		}
    	
		public Gps(Parcel in) {
			super(in);
		}

    }
    
    //sensor
    public static abstract class Sensor extends Stats {
    	public Sensor() {
		}
    	
		public Sensor(Parcel in) {
			super(in);
		}

    }
    
	public abstract void noteStartMethod(String callString, Throwable throwable, int hashCode);
	
	public abstract void noteStopMethod(int hashCode);

	public abstract void noteStartWakelock(String tag, Throwable throwable);
	
	public abstract void noteStopWakelock(String tag);
	
	public abstract void noteRequestLocationUpdates(String name,
			Throwable throwable);

	public abstract void noteRemoveUpdates(String name);
	
	public abstract void noteStartWifiScan(Throwable throwable);
	
	public abstract void noteStartMediaScan(Throwable throwable);
	
	public abstract void noteRegisterSensorListener(String listener, int type, Throwable throwable);
	
	public abstract void noteUnregisterSensorListener(String listener, int type);
}
