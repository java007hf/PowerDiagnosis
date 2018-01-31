package com.tencent.qrom.powerdiagnosis.common;

import android.os.Parcel;
import android.os.Parcelable;

public class MethodStatsImpl extends ProcessStats.MethodStats {
	public MethodStatsImpl(Parcel in) {
		super(in);
	}
	
	public MethodStatsImpl() {
	}
	
	public static final Parcelable.Creator<MethodStatsImpl> CREATOR =
        new Parcelable.Creator<MethodStatsImpl>() {
        @Override
        public MethodStatsImpl createFromParcel(Parcel in) {
            return new MethodStatsImpl(in);
        }

        @Override
        public MethodStatsImpl[] newArray(int size) {
            return new MethodStatsImpl[size];
        }
    };
}
