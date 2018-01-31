package com.tencent.qrom.powerdiagnosis.process;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.common.Constants;
import com.tencent.qrom.powerdiagnosis.process.ProcessTraceAdapter.OnActionListener;
import com.tencent.qrom.powerdiagnosis.process.ProcessTraceItem.StatusChangedObserver;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class ProcessTraceActivity extends Activity implements StatusChangedObserver {
	public static final String SHARED_PREFENCE_TAG = "SHARED_PREFENCE_TAG";
	private ListView mList;
	private ProcessTraceAdapter mAdapter;
	
	private static final int MSG_UPDATA_LISTVIEW = 0X001;
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATA_LISTVIEW:
				mAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};

	private OnActionListener mAdapterListener = new OnActionListener() {

		@Override
		public void onItemClick(ProcessTraceItem item) {
			LogHelper.v("ProcessTraceActivity.onItemClick : " + item);
			Intent intent = new Intent(ProcessTraceActivity.this,
					ProcessTraceDetailActivity.class);
			intent.putExtra(Constants.EXTRA_UID, item.getInfo().uid);
			intent.putExtra(Constants.EXTRA_PID, item.getInfo().pid);
			startActivity(intent);
		}

		@Override
		public void onItemCheckChanged(ProcessTraceItem item) {
			LogHelper.v("ProcessTraceActivity.onItemCheckChanged : " + item);
			if (item.isChecked())
				ProcessTraceManager.getInstance(getApplicationContext()).startTraceRequest(item, null);
			else
				ProcessTraceManager.getInstance(getApplicationContext()).stopTraceRequest(item, null);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_package_tracer);
		mList = (ListView) findViewById(R.id.list_package_tracer);
		mAdapter = new ProcessTraceAdapter(getApplicationContext(), this);
		mList.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdapter.updateData(ProcessTraceManager.getInstance(this).updateProcessTraceList());
		mAdapter.setListener(mAdapterListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAdapter.setListener(null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAdapter.detachAll();
		mList.setAdapter(null);
	}

	private void noteItemStatus(ProcessTraceItem p, boolean isEnabled) {
		SharedPreferences settings = getSharedPreferences(SHARED_PREFENCE_TAG, Context.MODE_PRIVATE);
	    SharedPreferences.Editor PE = settings.edit();
	    PE.putString(p.getInfo().processName, p.getInfo().pid + "_" + isEnabled);
	    PE.commit();
	}
	
	@Override
	public void onEnabledChanged(ProcessTraceItem p, boolean b) {
		mHandler.sendEmptyMessage(MSG_UPDATA_LISTVIEW);
	}

	@Override
	public void onCheckedChange(ProcessTraceItem p, boolean b) {
		mHandler.sendEmptyMessage(MSG_UPDATA_LISTVIEW);
		noteItemStatus(p, b);
	}

	@Override
	public void onPathChange(ProcessTraceItem p, String path) {
	}

	@Override
	public void onExtrChange(ProcessTraceItem p, Bundle bundle) {
	}
}
