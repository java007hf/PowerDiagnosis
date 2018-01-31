package com.tencent.qrom.powerdiagnosis.common;

import android.os.Parcel;
import android.os.Parcelable;

public class GpsImpl extends ProcessStats.Gps {
	public static final Parcelable.Creator<GpsImpl> CREATOR =
        new Parcelable.Creator<GpsImpl>() {
        @Override
        public GpsImpl createFromParcel(Parcel in) {
            return new GpsImpl(in);
        }

        @Override
        public GpsImpl[] newArray(int size) {
            return new GpsImpl[size];
        }
    };
	
	public GpsImpl(Parcel in) {
		super(in);
	}
	
	public GpsImpl() {
	}
}