package com.tencent.qrom.powerdiagnosis.common;

import android.os.Parcel;
import android.os.Parcelable;

public class WifiScanImpl extends ProcessStats.WifiScan {
	public static final Parcelable.Creator<WifiScanImpl> CREATOR =
        new Parcelable.Creator<WifiScanImpl>() {
        @Override
        public WifiScanImpl createFromParcel(Parcel in) {
            return new WifiScanImpl(in);
        }

        @Override
        public WifiScanImpl[] newArray(int size) {
            return new WifiScanImpl[size];
        }
    };
	
	public WifiScanImpl(Parcel in) {
		super(in);
	}
	
	public WifiScanImpl() {
	}
}