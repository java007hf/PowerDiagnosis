package com.tencent.qrom.powerdiagnosis.process;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.dataparse.DataCategory;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsData;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsInfo;

public class StatsFragment extends Fragment {
	private ListView mListView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_statsdatas, container, false);
		mListView = (ListView)view.findViewById(R.id.list_stats);
		
		updateList(view.getContext());
		return view;
	}
	
	private void updateList(Context context) {
		ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity)getActivity();
		List<DataCategory> dataCategories = processTraceDetailActivity.getDataCategories();
		if (dataCategories != null) {
			int[] stack = processTraceDetailActivity.getStack();
			int index = stack[ProcessTraceDetailActivity.LAYER_STATSDATA];
			int index2 = stack[ProcessTraceDetailActivity.LAYER_STATS];
			
			DataCategory dataCategory = dataCategories.get(index);
			List<StatsData> statsDatas = dataCategory.statsDatas;
			StatsData statsData = statsDatas.get(index2);
			
			StatsAdapter statsAdapter = new StatsAdapter(statsData.list, context);
			mListView.setAdapter(statsAdapter);
			OnItemClickListenerImpl m = new OnItemClickListenerImpl(statsData.list);
			mListView.setOnItemClickListener(m);
		}
	}
	
	class OnItemClickListenerImpl implements AdapterView.OnItemClickListener {
		List<StatsInfo> statsInfos;
		public OnItemClickListenerImpl(List<StatsInfo> statsInfos) {
			this.statsInfos = statsInfos;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity) getActivity();
			processTraceDetailActivity.showItems(position);
		}
	};
}
