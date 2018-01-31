package com.tencent.qrom.powerdiagnosis;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.qrom.powerdiagnosis.process.ProcessTraceActivity;
import com.tencent.qrom.powerdiagnosis.process.ProcessTraceService;
import com.tencent.qrom.powerdiagnosis.utils.XposedHelper;

public class MainActivity extends Activity {
	TextView mTextAppBatteryDrain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init_tmp();
		mTextAppBatteryDrain = (TextView)findViewById(R.id.function_package_tracer);
		mTextAppBatteryDrain.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, ProcessTraceActivity.class);
				startActivity(intent);			
			}
		});
		Intent intent = new Intent(MainActivity.this, ProcessTraceService.class);
		startService(intent);
	}
	
	private void init_tmp() {
		String path = XposedHelper.TMP_FOLDER + "/services.jar";
		File file = new File(path);
		if (!file.exists()) {
			XposedHelper.preCreateTmpFolder();
		}
	}
}
