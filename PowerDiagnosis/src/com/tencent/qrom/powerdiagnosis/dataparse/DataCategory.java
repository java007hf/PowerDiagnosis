package com.tencent.qrom.powerdiagnosis.dataparse;

import java.util.ArrayList;
import java.util.List;

public class DataCategory {
	public long totalTime;
	public long totalCount;
	public List<StatsData> statsDatas = new ArrayList<StatsData>();
	public String category;
	
	public DataCategory(String category) {
		this.category = category;
	}
}
