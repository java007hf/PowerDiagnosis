package com.example.testtrace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements MediaScannerConnectionClient {
	private static final String TAG = "MainActivity";
	private Button mTestCPUButton;
	private Button mTestAlarmButton;
	private Button mTestGPSButton;
	private Button mTestSensorButton;
	private Button mTestWakeLockButton;
	private Button mTestWifiScanButton;
	private Button mMediaScanButton;
	private LocationManager locationManager = null;
	private SensorManager sm = null;
	private PowerManager pm = null;
	private PowerManager.WakeLock wl = null;
	private WifiManager mainWifi;
	private WifiReceiver receiverWifi;
	
	private Context mContext;

	private MediaScannerConnection mConnection;
	
	private static final int MSG_CLOSED_GPS = 0X01;
	private static final int MSG_CLOSED_SENSOR = 0X02;
	private static final int MSG_CLOSED_WAKELOCK = 0X03;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CLOSED_GPS:
				locationManager.removeUpdates(mLocationListener);
				break;
			case MSG_CLOSED_SENSOR:
				sm.unregisterListener(mSensorEventListener);
				break;
			case MSG_CLOSED_WAKELOCK:
				if (wl.isHeld())
					wl.release();
				break;
			default:
				break;
			}
		};
	};

	private void testCpu() {
		Log.d(TAG, "==========testCpu=========");
		funA();
	}

	private void testAlarm() {
		Log.d(TAG, "==========testAlarm=========");
	}

	private void testGps() {
		Log.d(TAG, "==========testGps=========");

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			new AlertDialog.Builder(this)
					.setTitle("地圖工具")
					.setMessage("您尚未開啟定位服務，要前往設定頁面啟動定位服務嗎？")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									startActivity(new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS));
								}

							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(MainActivity.this,
											"未開啟定位服務，無法使用本工具!!",
											Toast.LENGTH_SHORT).show();
								}

							}).show();
		} else {
//			locationManager.requestLocationUpdates(
//					LocationManager.NETWORK_PROVIDER, 1000, 0,
//					mLocationListener);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);

			mHandler.sendEmptyMessageDelayed(MSG_CLOSED_GPS, 5000);
		}
	}

	class WifiReceiver extends BroadcastReceiver {

		public void onReceive(Context c, Intent intent) {
			StringBuilder sb = new StringBuilder();
			sb = new StringBuilder();
			List<ScanResult> wifiList = mainWifi.getScanResults();

			for (int i = 0; i < wifiList.size(); i++) {

				sb.append(new Integer(i + 1).toString() + ".");
				sb.append((wifiList.get(i)).toString());
				sb.append("\n\n");
			}
			Log.d(TAG, sb.toString());
		}
	}

	private LocationListener mLocationListener = new MyLocationListener();

	class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "onLocationChanged " + location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "onProviderDisabled " + provider);
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "onProviderEnabled " + provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "onStatusChanged " + status);
		}
	}

	private void testSensor() {
		Log.d(TAG, "==========testSensor=========");
		int sensorType = Sensor.TYPE_ACCELEROMETER;
		sm.registerListener(mSensorEventListener,
				sm.getDefaultSensor(sensorType),
				SensorManager.SENSOR_DELAY_NORMAL);

		mHandler.sendEmptyMessageDelayed(MSG_CLOSED_SENSOR, 5000);
	}

	private void testWakeLock() {
		Log.d(TAG, "==========testWakeLock=========");
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
		wl.acquire();
		mHandler.sendEmptyMessageDelayed(MSG_CLOSED_WAKELOCK, 5000);
	}

	private void testWifiScan() {
		Log.d(TAG, "==========testWifiScan=========");
		if (!mainWifi.isWifiEnabled()) {
			mainWifi.setWifiEnabled(true);
		}
		mainWifi.startScan();
	}

	SensorEventListener mSensorEventListener = new SensorEventListener() {

		// 复写onSensorChanged方法
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				Log.i(TAG, "onSensorChanged");

				// 图解中已经解释三个值的含义
				float X_lateral = sensorEvent.values[0];
				float Y_longitudinal = sensorEvent.values[1];
				float Z_vertical = sensorEvent.values[2];
				Log.i(TAG, "\n heading " + X_lateral);
				Log.i(TAG, "\n pitch " + Y_longitudinal);
				Log.i(TAG, "\n roll " + Z_vertical);
			}
		}

		// 复写onAccuracyChanged方法
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			Log.i(TAG, "onAccuracyChanged");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		mTestCPUButton = (Button) findViewById(R.id.testcpu);
		mTestCPUButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testCpu();
			}
		});

		mTestAlarmButton = (Button) findViewById(R.id.testalarm);
		mTestAlarmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testAlarm();
			}
		});

		mTestGPSButton = (Button) findViewById(R.id.testgps);
		mTestGPSButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testGps();
			}
		});

		mTestWifiScanButton = (Button) findViewById(R.id.testwifiscan);
		mTestWifiScanButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testWifiScan();
			}
		});

		mTestSensorButton = (Button) findViewById(R.id.testsensor);
		mTestSensorButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testSensor();
			}
		});

		mTestWakeLockButton = (Button) findViewById(R.id.testwakelock);
		mTestWakeLockButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				testWakeLock();
			}
		});
		
		mConnection = new MediaScannerConnection(this, this);
		mConnection.connect();
		
		mMediaScanButton = (Button)findViewById(R.id.testmediascan);
		mMediaScanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				getFiles();
				
				if (mConnection.isConnected()) {
					mConnection.scanFile("/sdcard/", null);
					
					String saveAs = "/sdcard/";
					Uri contentUri = Uri.fromFile(new File(saveAs));
					Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
					mContext.sendBroadcast(mediaScanIntent);
				}
			}
		});
	}

	private List<String> getFiles() {
		Log.d(TAG, "====getFiles====");
		
		List<String> fileStrings = new ArrayList<String>();
		File cacheFile = getCacheDir();
		File listFile[] = cacheFile.listFiles();
		
		for(File file : listFile) {
			String name = file.getName();
			if (name!=null && name.startsWith("process_info")) {
				Log.d(TAG, file.getName());
				fileStrings.add(name);
			}
		}
		
		return fileStrings;
	}
	
	private void compute() {
		int sum = 0;
		for (int i = 0; i < 10; i++) {
			sum = sum + i;
			Log.d("ben", "i = " + i + " sum == " + sum);
		}

		Log.d("ben", "sum 1111111111== " + sum);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	int i = 0;

	private void funA() {
		Log.d("ben", "sum 111111funA1111== ");
		i++;
		compute();
		if (i < 20) {
			funB();
		} else {
			i = 0;
		}
	}

	private void funB() {
		Log.d("ben", "sum 11111funB11111== ");
		compute();
		funA();
	}

	@Override
	public void onMediaScannerConnected() {
	}

	@Override
	public void onScanCompleted(String arg0, Uri arg1) {
		Log.d("xxx", "onScanCompleted " + arg0 + " " + arg1);
		
	}
}
