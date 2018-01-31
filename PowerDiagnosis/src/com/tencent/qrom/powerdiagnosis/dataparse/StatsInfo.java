package com.tencent.qrom.powerdiagnosis.dataparse;

import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Stats;

public class StatsInfo {
	public String name;
	public Stats stats;
	
	public StatsInfo(Stats stats, String name) {
		this.name = name;
		this.stats = stats;
	}
}
