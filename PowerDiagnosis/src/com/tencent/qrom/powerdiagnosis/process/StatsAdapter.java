package com.tencent.qrom.powerdiagnosis.process;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsInfo;

public class StatsAdapter extends BaseAdapter {
	private List<StatsInfo> mStatsInfos;
	private LayoutInflater mInflater;
	
	public StatsAdapter(List<StatsInfo> list, Context context) {
		mStatsInfos = list;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mStatsInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mStatsInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_stats, null);
			holder = new ViewHolder();
			holder.textTextView = (TextView) convertView.findViewById(R.id.item_text);
			holder.totalTimeTextView = (TextView) convertView.findViewById(R.id.totaltime_text);
			holder.totalCountTextView = (TextView) convertView.findViewById(R.id.totalcount_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		StatsInfo statsData = (StatsInfo) getItem(position);
		holder.textTextView.setTextSize(10);
		holder.textTextView.setText(statsData.name);
		holder.totalTimeTextView.setText("totalTime : " + statsData.stats.getInfo().getTotalTime());
		holder.totalCountTextView.setText("totalCount : " + statsData.stats.getInfo().getTotalCount());
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView textTextView;
		TextView totalTimeTextView;
		TextView totalCountTextView;
	}

}
