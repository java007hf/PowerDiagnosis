package com.tencent.qrom.powerdiagnosis.common;

import android.os.Parcel;
import android.os.Parcelable;

public class WakelockImpl extends ProcessStats.Wakelock {
	public static final Parcelable.Creator<WakelockImpl> CREATOR =
        new Parcelable.Creator<WakelockImpl>() {
        @Override
        public WakelockImpl createFromParcel(Parcel in) {
            return new WakelockImpl(in);
        }

        @Override
        public WakelockImpl[] newArray(int size) {
            return new WakelockImpl[size];
        }
    };
	
	public WakelockImpl(Parcel in) {
		super(in);
	}
	
	public WakelockImpl() {
	}
}