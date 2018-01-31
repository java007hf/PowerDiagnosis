package com.tencent.qrom.powerdiagnosis.process;

import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Item;
import com.tencent.qrom.powerdiagnosis.dataparse.DataCategory;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsData;
import com.tencent.qrom.powerdiagnosis.dataparse.StatsInfo;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class ItemFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		TextView textView = new TextView(container.getContext());
		textView.setTextSize(8);
		ProcessTraceDetailActivity p = (ProcessTraceDetailActivity)getActivity();
		int[] stack = p.getStack();
		List<DataCategory> categories = p.getDataCategories();
		
		LogHelper.d("ItemFragment " + categories);
		if (categories != null) {
			DataCategory dataCategory = categories.get(stack[ProcessTraceDetailActivity.LAYER_STATSDATA]);
			StatsData statsData = dataCategory.statsDatas.get(stack[ProcessTraceDetailActivity.LAYER_STATS]);
			StatsInfo statsInfo = statsData.list.get(stack[ProcessTraceDetailActivity.LAYER_ITEM]);
			
			int maxIndex = statsInfo.stats.getInfo().mMaxItemIndex;
			if (statsInfo.stats.getInfo().mItems.size() > maxIndex) {
				Item item = statsInfo.stats.getInfo().mItems.get(maxIndex);
				
				if (item.mStackTraces.size() > 0) {
					String stackString = item.mStackTraces.get(0);
					LogHelper.d("stackString " + stackString);
					textView.setText(stackString);
				} else {
					textView.setText("NULL");
				}
			} else {
				textView.setText("NULL");
			}
		}
		
		textView.setOnKeyListener(backlistener);
		return textView;
	}
	
	private View.OnKeyListener backlistener = new View.OnKeyListener() {  
        @Override  
        public boolean onKey(View view, int i, KeyEvent keyEvent) {  
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            	ProcessTraceDetailActivity processTraceDetailActivity = (ProcessTraceDetailActivity)getActivity();
            	int[] stack = processTraceDetailActivity.getStack();
            	processTraceDetailActivity.showStatses(stack[ProcessTraceDetailActivity.LAYER_STATS]);
            	return true;
            }  
            return false;  
        }
    }; 
}
