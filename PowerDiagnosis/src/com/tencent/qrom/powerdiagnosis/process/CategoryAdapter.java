package com.tencent.qrom.powerdiagnosis.process;

import java.util.List;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.dataparse.DataCategory;
import com.tencent.qrom.powerdiagnosis.dataparse.DataParse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CategoryAdapter extends BaseAdapter {
	private List<DataCategory> mCategories;
	private LayoutInflater mInflater;
	
	public CategoryAdapter(List<DataCategory> categories, Context context) {
		mCategories = categories;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mCategories.size();
	}

	@Override
	public Object getItem(int position) {
		return mCategories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_category, null);
			holder = new ViewHolder();
			holder.textTextView = (TextView) convertView.findViewById(R.id.item_text);
			holder.totalTimeTextView = (TextView) convertView.findViewById(R.id.totaltime_text);
			holder.totalCountTextView = (TextView) convertView.findViewById(R.id.totalcount_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		DataCategory dataCategory = (DataCategory) getItem(position);
		holder.textTextView.setText(dataCategory.category);
		
		if (dataCategory.totalTime == DataParse.DATA_NONE) {
			holder.totalTimeTextView.setText("totalTime : NONE");
		} else {
			holder.totalTimeTextView.setText("totalTime : " + dataCategory.totalTime);
		}
		
		holder.totalCountTextView.setText("totalCount : " + dataCategory.totalCount);
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView textTextView;
		TextView totalTimeTextView;
		TextView totalCountTextView;
	}

}
