package com.tencent.qrom.powerdiagnosis.process;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.common.ProcessStats;
import com.tencent.qrom.powerdiagnosis.dataparse.DataCategory;
import com.tencent.qrom.powerdiagnosis.dataparse.DataParse;
import com.tencent.qrom.powerdiagnosis.process.ProcessTraceItem.StatusChangedObserver;
import com.tencent.qrom.powerdiagnosis.utils.Helpers;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class CategoryFragment extends Fragment implements StatusChangedObserver {
	private TextView mTitle;
	private Button mButtonEnabled;
	private Button mButtonClear;
	private Button mButtonFileList;
	private Button mButtonClearFile;
	private TextView mStartTimeTextView;
	private TextView mStopTimeTextView;
	private ListView mDietaiListView;
	
	private static final int MSG_ENABLE_CHANGE = 0X01;
	private static final int MSG_CHECK_CHANGE = 0X02;
	private static final int MSG_PATH_CHANGE = 0X03;
	
	private static final String THREADNAME = "CategoryFragmentThread";
	private ProcessTraceItem mItem;
	private Handler mHandlerThread;
	
	private static final int MSG_PARSE_DATA = 0x01;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_ENABLE_CHANGE:
				boolean b = (Boolean) msg.obj;
				updateEnableStatus(b);
				break;
			case MSG_CHECK_CHANGE:
				boolean checkflag = (Boolean) msg.obj;
				updateButton(checkflag);
				break;
			case MSG_PATH_CHANGE:
				long startTime = mItem.getStartRecordTime();
				long stopTime = mItem.getStopRecordTime();
				
				mStartTimeTextView.setText("startTime: " + getTime(startTime));
				mStopTimeTextView.setText("stopTime: " + getTime(stopTime));
				updateList(getView().getContext());
				break;
			default:
				break;
			}
		};
	};
	
	private void updateList(Context context) {
		ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity)getActivity();
		List<DataCategory> dataCategories = processTraceDetailActivity.getDataCategories();
		if (dataCategories != null) {
			CategoryAdapter categoryAdapter = new CategoryAdapter(dataCategories, context);
			mDietaiListView.setAdapter(categoryAdapter);
			OnItemClickListenerImpl m = new OnItemClickListenerImpl(dataCategories);
			mDietaiListView.setOnItemClickListener(m);
		}
	}
	
	public void setItem(ProcessTraceItem item) {
		Log.d("ben", "========setItem===========" + item);
		mItem = item;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		HandlerThread handlerThread = new HandlerThread(THREADNAME);
		handlerThread.start();
		mHandlerThread = new Handler(handlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_PARSE_DATA:
					String path = (String)msg.obj;
					ProcessStats p = Helpers.getProcessStats(path);
//					p.dump();
					List<DataCategory> dataCategories = DataParse.getInstance().getDataCategories(p);
					ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity)getActivity();
					processTraceDetailActivity.setDataCategories(dataCategories);
					
					mHandler.sendEmptyMessage(MSG_PATH_CHANGE);
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogHelper.d("========onCreateView==========="+mItem);
		final View view = inflater.inflate(R.layout.fragment_category, container, false);
		
		mTitle = (TextView)view.findViewById(R.id.title_package_tracer_detail);
		mButtonEnabled = (Button) view.findViewById(R.id.button_enable);
		mButtonEnabled.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItem.isChecked()) {
					mItem.setChecked(false);
					ProcessTraceManager.getInstance(view.getContext()).stopTraceRequest(mItem, null);
				} else {
					mItem.setChecked(true);
					ProcessTraceManager.getInstance(view.getContext()).startTraceRequest(mItem, null);
				}
				
				mItem.setEnabled(false);
			}
		});
	
		mButtonClear = (Button) view.findViewById(R.id.button_clear);
		mButtonClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ProcessTraceManager.getInstance(view.getContext()).clearTraceRequest(mItem, null);
				mItem.setEnabled(false);
			}
		});
		
		mButtonFileList = (Button)view.findViewById(R.id.button_filelist);
		mButtonFileList.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString(ProcessTraceManager.GENERAL_COMMAND_KEY, ProcessTraceManager.COMMAND_FILELIST);
				ProcessTraceManager.getInstance(view.getContext()).generalTraceRequest(mItem, bundle);
			}
		});
		
		mButtonClearFile = (Button)view.findViewById(R.id.button_clearfile);
		mButtonClearFile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString(ProcessTraceManager.GENERAL_COMMAND_KEY, ProcessTraceManager.COMMAND_CLEARFILE);
				ProcessTraceManager.getInstance(view.getContext()).generalTraceRequest(mItem, bundle);
			}
		});
		
		mStartTimeTextView = (TextView)view.findViewById(R.id.startTime);
		mStopTimeTextView = (TextView)view.findViewById(R.id.stopTime);
		mDietaiListView = (ListView)view.findViewById(R.id.list_detail);
		
		mTitle.setText(String.format("%5d   ", mItem.getInfo().pid) + mItem.getInfo().processName);
		updateButton(mItem.isChecked());
		if (mItem.isChecked()) {
			updateEnableStatus(mItem.isEnabled());
		}
		
		ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity)getActivity();
		List<DataCategory> dataCategories = processTraceDetailActivity.getDataCategories();
		
		if (dataCategories == null) {
//			if (!mItem.getPath().equals("")) {
//				mHandler.sendEmptyMessage(MSG_PATH_CHANGE);
//			}
		} else {
			long startTime = mItem.getStartRecordTime();
			long stopTime = mItem.getStopRecordTime();
			mStartTimeTextView.setText("startTime: " + getTime(startTime));
			mStopTimeTextView.setText("stopTime: " + getTime(stopTime));
		}
		
		mItem.attach(this);
		
		updateList(view.getContext());
		return view;
	}
	
	@Override
	public void onDestroyView() {
		mItem.detach(this);
		super.onDestroyView();
	}
	
	class OnItemClickListenerImpl implements AdapterView.OnItemClickListener {
		List<DataCategory> dataCategories;
		public OnItemClickListenerImpl(List<DataCategory> dataCategories) {
			this.dataCategories = dataCategories;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity) getActivity();
			processTraceDetailActivity.showStatsDatas(position);
		}
	};
	
	private String getTime(long m) {
		Date date = new Date(m);
		SimpleDateFormat sdFormatter = new SimpleDateFormat("MM-dd HH:mm:ss");  
		String retStrFormatNowDate = sdFormatter.format(date);
		return retStrFormatNowDate;
	}
	
	@Override
	public void onEnabledChanged(ProcessTraceItem p, boolean b) {
		Message message = mHandler.obtainMessage(MSG_ENABLE_CHANGE);
		message.obj = b;
		mHandler.sendMessage(message);
	}

	@Override
	public void onCheckedChange(ProcessTraceItem p, boolean b) {
		Message message = mHandler.obtainMessage(MSG_CHECK_CHANGE);
		message.obj = b;
		mHandler.sendMessage(message);
	}
	
	private void updateEnableStatus(boolean b) {
		mButtonEnabled.setEnabled(b);
//		mButtonClear.setEnabled(b);
	}
	
	private void updateButton(boolean b) {
		if (b) {
			mButtonEnabled.setText(getString(R.string.stop));
			mButtonClear.setEnabled(true);
			mButtonClearFile.setEnabled(true);
			mButtonFileList.setEnabled(true);
		} else {
			mButtonEnabled.setText(getString(R.string.start));
			mButtonClear.setEnabled(false);
			mButtonClearFile.setEnabled(false);
			mButtonFileList.setEnabled(false);
		}
	}

	@Override
	public void onPathChange(ProcessTraceItem p, String path) {
		openFile(path);
	}
	
	@Override
	public void onExtrChange(ProcessTraceItem p, Bundle bundle) {
		String action = bundle.getString(ProcessTraceManager.GENERAL_COMMAND_KEY);
		if (ProcessTraceManager.COMMAND_FILELIST.equals(action)) {
			String[] files = bundle.getStringArray("files");
			if (files != null) {
				listFiles(files);
			}
		}
	}
	
	private void openFile(String path) {
		Message message = mHandlerThread.obtainMessage(MSG_PARSE_DATA);
		message.obj = path;
		mHandlerThread.sendMessage(message);
	}  
	
	private void listFiles(final String[] files) {
		LogHelper.d("====listFiles====");
		
		if (files.length > 0) {
			AlertDialog.Builder listDia=new AlertDialog.Builder(getActivity());  
			listDia.setItems(files, new DialogInterface.OnClickListener() {  
				
				@Override  
				public void onClick(DialogInterface dialog, int which) {  
					openFile(files[which]);
				}
			});  
			listDia.create().show();  
		} else {
			Toast.makeText(getActivity(), R.string.noFile, Toast.LENGTH_SHORT).show();
		}
	}
}
