package com.tencent.qrom.powerdiagnosis.process;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.process.ProcessTraceItem.StatusChangedObserver;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

class ProcessTraceAdapter extends ArrayAdapter<ProcessTraceItem>
		implements OnCheckedChangeListener, OnClickListener {
	private LayoutInflater mInflater;
	private OnActionListener mListener;
	private StatusChangedObserver mChangedObserver;
	private List<ProcessTraceItem> mLastList = null;

	static interface OnActionListener {
		void onItemCheckChanged(ProcessTraceItem info);
		void onItemClick(ProcessTraceItem info);
	}

	public ProcessTraceAdapter(Context context, StatusChangedObserver o) {
		super(context, 0);
		mChangedObserver = o;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void updateData(List<ProcessTraceItem> list) {
		detachAll();
		clear();
		addAll(list);
		
		for(ProcessTraceItem item : list) {
			item.attach(mChangedObserver);
		}
		mLastList = list;
		notifyDataSetChanged();
	}
	
	public void detachAll() {
		if (mLastList != null) {
			for(ProcessTraceItem item : mLastList) {
				item.detach(mChangedObserver);
			}
		}
	}

	public void setListener(OnActionListener listener) {
		mListener = listener;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_process, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.item_text);
			holder.text.setOnClickListener(this);
			holder.sw = (Switch) convertView.findViewById(R.id.item_swicth);
			holder.sw.setOnCheckedChangeListener(this);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ProcessTraceItem item = getItem(position);
		holder.text.setTag(position);
		holder.sw.setTag(position);
		holder.text.setText(item.getInfo().processName);	
		holder.sw.setChecked(item.isChecked());
		holder.sw.setEnabled(item.isEnabled());

		return convertView;
	}

	private static class ViewHolder {
		TextView text;
		Switch sw;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int position = (Integer) buttonView.getTag();
		ProcessTraceItem item = getItem(position);
		if (item.isChecked() != isChecked) {
			item.setChecked(isChecked);
			LogHelper.d("onCheckedChanged " + false);
			item.setEnabled(false);
			mListener.onItemCheckChanged(item);
			notifyDataSetChanged();
		}
	}
 
	@Override
	public void onClick(View v) {
		LogHelper.v("onClick");
		int position = (Integer) v.getTag();
		mListener.onItemClick(getItem(position));
	}
}
