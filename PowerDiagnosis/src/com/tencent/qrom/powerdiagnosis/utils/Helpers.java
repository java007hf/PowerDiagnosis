package com.tencent.qrom.powerdiagnosis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Parcel;

import com.tencent.qrom.powerdiagnosis.common.ProcessStats;
import com.tencent.qrom.powerdiagnosis.common.ProcessStatsImpl;

public class Helpers {
	public static boolean extractAssetsToLocal(Context context, String srcName, String outPath) {
		boolean ret = false;
        InputStream is = null;  
        OutputStream os = null;
		try {
			is = context.getAssets().open(srcName);
			os = new FileOutputStream(outPath);
	        byte[] buffer = new byte[1024];  
	        int len = -1;
	        while((len = is.read(buffer)) != -1) {
	            os.write(buffer, 0, len); 
	        }
	        os.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
        	try {
        		if(os != null) {
        			os.close();
        		}
        		File f = new File(outPath);
    			if(f.exists()) {
    				f.setReadable(true, false);
    			}
        		if(is != null) {
        			is.close();
        		}
        		ret = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ret;
    }
	
	private static int toInt(byte[] bRefArr) {
	    int iOutcome = 0;
	    byte bLoop;

	    for (int i = 0; i < bRefArr.length; i++) {
	        bLoop = bRefArr[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }
	    return iOutcome;
	}
	
	public static ProcessStats getProcessStats(String path) {
		byte[] data = new byte[0];
		try {
			FileInputStream fileInputStream = new FileInputStream(path);
			byte[] sizeb = new byte[4];
			fileInputStream.read(sizeb);
			int size = toInt(sizeb);
			LogHelper.d("getProcessStats path = " + path + ", size = " + size);
			
			data = new byte[size];
			fileInputStream.read(data);
			fileInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		
		ProcessStats processStats = new ProcessStatsImpl(parcel);
		parcel.recycle();
//		processStats.dump();
		return processStats;

	}
}
