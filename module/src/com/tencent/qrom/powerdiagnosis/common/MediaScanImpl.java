package com.tencent.qrom.powerdiagnosis.common;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaScanImpl extends ProcessStats.MediaScan {
	public static final Parcelable.Creator<MediaScanImpl> CREATOR =
        new Parcelable.Creator<MediaScanImpl>() {
        @Override
        public MediaScanImpl createFromParcel(Parcel in) {
            return new MediaScanImpl(in);
        }

        @Override
        public MediaScanImpl[] newArray(int size) {
            return new MediaScanImpl[size];
        }
    };
	
	public MediaScanImpl(Parcel in) {
		super(in);
	}
	
	public MediaScanImpl() {
	}
}
