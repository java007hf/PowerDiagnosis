package com.tencent.powerhook;

import android.os.SystemProperties;

public  class PlugUtils {

	public static boolean isMIUIAnd4_2(){
//		android.os.MO
		String model = android.os.Build.MODEL;
		int sdkVersion = getCurrentSdkVersion();
		if(model.contains("MI") &&sdkVersion==17){
			return true;
		}
		return false;
	}
	
	public static String getCurAndroidVersion() {
		String version = SystemProperties.get("ro.build.version.release");
		return version;
	}
	
	public static  int getCurrentSdkVersion(){
		return android.os.Build.VERSION.SDK_INT;
	}
	
	public static  boolean isMIUI(){
		String brand = SystemProperties.get("ro.product.brand");
		return brand.contains("Xiaomi") || brand.contains("xiaomi");
	}
}
