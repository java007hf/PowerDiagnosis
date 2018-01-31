package com.tencent.qrom.powerdiagnosis.process;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tencent.qrom.powerdiagnosis.R;
import com.tencent.qrom.powerdiagnosis.common.Constants;
import com.tencent.qrom.powerdiagnosis.dataparse.DataCategory;
import com.tencent.qrom.powerdiagnosis.dataparse.DataParse;
import com.tencent.qrom.powerdiagnosis.utils.LogHelper;

public class ProcessTraceDetailActivity extends Activity {
	private ProcessTraceItem mItem;
	private List<DataCategory> mDataCategories;
	private CategoryFragment mCategoryFragment;
	private int[] mStack = new int[4]; //category-type-stats-item
	public static final int LAYER_CATEGORY = 0;
	public static final int LAYER_STATSDATA = 1;
	public static final int LAYER_STATS = 2;
	public static final int LAYER_ITEM = 3;
	
	private int mCurrentStack = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent == null) finish();
		int uid = intent.getIntExtra(Constants.EXTRA_UID, 0);
		int pid = intent.getIntExtra(Constants.EXTRA_PID, 0);
		mItem = ProcessTraceManager.getInstance(this).findProcessItem(uid, pid);
		if (mItem == null) finish();
		setContentView(R.layout.activity_package_tracer_detail);
		setDefaultFragment();
	}
	
	public int[] getStack() {
		return mStack;
	}
	
	public void setDataCategories(List<DataCategory> dataCategories) {
		mDataCategories = dataCategories;
	}
	
	public void setCurrentStack(int currentStack) {
		mCurrentStack = currentStack;
	}
	
	public List<DataCategory> getDataCategories() {
		return mDataCategories;
	}

	private void setDefaultFragment()  
    {  
		mStack[LAYER_CATEGORY] = 1;
		setCurrentStack(LAYER_CATEGORY);
        FragmentManager fm = getFragmentManager();  
        FragmentTransaction transaction = fm.beginTransaction();
        
        if (mCategoryFragment == null) {
        	mCategoryFragment = new CategoryFragment();
        }
        mCategoryFragment.setItem(mItem);
        transaction.replace(R.id.fragment_connect, mCategoryFragment);  
        transaction.commit();  
    }
	
	public void showStatses(int position) {
		int categoryIndex = mStack[LAYER_STATSDATA];
		DataCategory dataCategory = mDataCategories.get(categoryIndex);
		String category = dataCategory.category;
		if (category.equals(DataParse.CATEGORY_CPU)) {
			int size = dataCategory.statsDatas.get(position).list.size();
			if (size == 0) return;
		} else if (category.equals(DataParse.CATEGORY_SENSOR)) {
			int size = dataCategory.statsDatas.get(position).list.size();
			if (size == 0) return;
		} else if (category.equals(DataParse.CATEGORY_WAKELOCK)) {
			int size = dataCategory.statsDatas.get(0).list.size();
			if (size == 0) return;
		} else if (category.equals(DataParse.CATEGORY_GPS)) {
			int size = dataCategory.statsDatas.get(0).list.size();
			if (size == 0) return;
		} else if (category.equals(DataParse.CATEGORY_WIFISCAN)
				|| category.equals(DataParse.CATEGORY_MEDIASCAN)) {
			mStack[LAYER_STATS] = position;
			setCurrentStack(LAYER_STATS);
			showItems(0);
			return;
		} 
		
		mStack[LAYER_STATS] = position;
		setCurrentStack(LAYER_STATS);
		FragmentManager fm = getFragmentManager();  
        FragmentTransaction transaction = fm.beginTransaction();  
        StatsFragment statsFragment = new StatsFragment();
        transaction.replace(R.id.fragment_connect, statsFragment);  
        transaction.commit(); 
	}
	
	public void showItems(int position) {
		int categoreIndex = mStack[LAYER_STATSDATA];
		int dataIndex = mStack[LAYER_STATS];
		int itemIndex = mStack[LAYER_ITEM];
		
		int size = mDataCategories.get(categoreIndex).statsDatas.get(dataIndex).list.get(itemIndex).stats.getInfo().mItems.size();
		if (size == 0) return;
		mStack[LAYER_ITEM] = position;
		setCurrentStack(LAYER_ITEM);
		FragmentManager fm = getFragmentManager();  
        FragmentTransaction transaction = fm.beginTransaction();
        ItemFragment itemFragment = new ItemFragment();
        transaction.replace(R.id.fragment_connect, itemFragment);  
        transaction.commit();
	}
	
	public void showStatsDatas(int position) {
		DataCategory dataCategory = mDataCategories.get(position);
		String category = dataCategory.category;
		
		if (category.equals(DataParse.CATEGORY_CPU)) {
			int size = dataCategory.statsDatas.size();
			if (size == 0) return;
		} else if (category.equals(DataParse.CATEGORY_SENSOR)) {
			int size = dataCategory.statsDatas.size();
			if (size == 0) return;
		} else if (category.equals(DataParse.CATEGORY_WAKELOCK)) {
			mStack[LAYER_STATSDATA] = position;
			setCurrentStack(LAYER_STATSDATA);
			
			showStatses(0);
			return;
		} else if (category.equals(DataParse.CATEGORY_GPS)) {
			mStack[LAYER_STATSDATA] = position;
			setCurrentStack(LAYER_STATSDATA);
			
			showStatses(0);
			return;
		} else if (category.equals(DataParse.CATEGORY_WIFISCAN)
				|| category.equals(DataParse.CATEGORY_MEDIASCAN)) {
			mStack[LAYER_STATSDATA] = position;
			setCurrentStack(LAYER_STATSDATA);
			
			showItems(0);
			return;
		}
		
		mStack[LAYER_STATSDATA] = position;
		setCurrentStack(LAYER_STATSDATA);
		
		FragmentManager fm = getFragmentManager();  
        FragmentTransaction transaction = fm.beginTransaction();  
        StatsDatasFragment statsDatasFragment = new StatsDatasFragment();
        transaction.replace(R.id.fragment_connect, statsDatasFragment);  
        transaction.commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean isDone = false;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			int stackIndex = mCurrentStack;
			int lastIndex = stackIndex - 1;
			
			if (mCurrentStack > LAYER_CATEGORY) {
				int categoryIndex = mStack[LAYER_STATSDATA];
				DataCategory dataCategory = mDataCategories.get(categoryIndex);
				String category = dataCategory.category;
				if (category.equals(DataParse.CATEGORY_CPU)) {
				} else if (category.equals(DataParse.CATEGORY_SENSOR)) {
				} else if (category.equals(DataParse.CATEGORY_WAKELOCK)) {
					if (stackIndex == LAYER_STATS) {
						lastIndex = LAYER_CATEGORY;
					}
				} else if (category.equals(DataParse.CATEGORY_GPS)) {
					if (stackIndex == LAYER_STATS || stackIndex == LAYER_STATSDATA) {
						lastIndex = LAYER_CATEGORY;
					}
				} else if (category.equals(DataParse.CATEGORY_WIFISCAN)
						|| category.equals(DataParse.CATEGORY_MEDIASCAN)) {
					if (stackIndex != LAYER_CATEGORY) {
						lastIndex = LAYER_CATEGORY;
					}
				}
			}
			
			LogHelper.d("onKeyDown stackIndex " + stackIndex);
			LogHelper.d("onKeyDown lastIndex " + lastIndex);
			
			switch (lastIndex) {
			case LAYER_CATEGORY:
				setDefaultFragment();
				isDone = true;
				break;
			case LAYER_STATSDATA:
				showStatsDatas(mStack[LAYER_STATSDATA]);
				isDone = true;
				break;
			case LAYER_STATS:
				showStatses(mStack[LAYER_STATS]);
				isDone = true;
				break;
			default:
				break;
			}
		}
		
		LogHelper.d("onKeyDown isDone " + isDone);
		return isDone?isDone:super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		if (mDataCategories != null) {
			mDataCategories.clear();
		}
		Arrays.fill(mStack, 0);
		super.onDestroy();
	}
}
