package com.tencent.qrom.powerdiagnosis.dataparse;

import java.util.ArrayList;
import java.util.List;

public class StatsData implements Comparable {
	public String name;
	public long totalTime;
	public long totalCount;
	public List<StatsInfo> list = new ArrayList<StatsInfo>();
	public String type;
	
	public StatsData(String type) {
		this.type = type;
	}
	
	@Override
	public int compareTo(Object obj) {
		StatsData b = (StatsData) obj;  
        return (int) (b.totalTime - this.totalTime);
	}
}
