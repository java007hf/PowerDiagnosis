package com.tencent.qrom.powerdiagnosis.process;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsData;

public class StatsDatasAdapter extends BaseAdapter {
	private List<StatsData> mStatsDatas;
	private LayoutInflater mInflater;
	
	public StatsDatasAdapter(List<StatsData> statsDatas, Context context) {
		mStatsDatas = statsDatas;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mStatsDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mStatsDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_statsdata, null);
			holder = new ViewHolder();
			holder.textTextView = (TextView) convertView.findViewById(R.id.item_text);
			holder.totalTimeTextView = (TextView) convertView.findViewById(R.id.totaltime_text);
			holder.totalCountTextView = (TextView) convertView.findViewById(R.id.totalcount_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		StatsData statsData = (StatsData) getItem(position);
		holder.textTextView.setText(statsData.name);
		holder.totalTimeTextView.setText("totalTime : " + statsData.totalTime);
		holder.totalCountTextView.setText("totalCount : " + statsData.totalCount);
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView textTextView;
		TextView totalTimeTextView;
		TextView totalCountTextView;
	}

}
