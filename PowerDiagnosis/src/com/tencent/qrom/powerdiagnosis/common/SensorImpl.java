package com.tencent.qrom.powerdiagnosis.common;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorImpl extends ProcessStats.Sensor {
	public static final Parcelable.Creator<SensorImpl> CREATOR =
        new Parcelable.Creator<SensorImpl>() {
        @Override
        public SensorImpl createFromParcel(Parcel in) {
            return new SensorImpl(in);
        }

        @Override
        public SensorImpl[] newArray(int size) {
            return new SensorImpl[size];
        }
    };
	
	public SensorImpl(Parcel in) {
		super(in);
	}
	
	public SensorImpl() {
	}
}