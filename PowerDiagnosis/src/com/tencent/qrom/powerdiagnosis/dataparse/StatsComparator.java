package com.tencent.qrom.powerdiagnosis.dataparse;

import java.util.Comparator;

import com.tencent.qrom.powerdiagnosis.common.ProcessStats.Stats;

public class StatsComparator implements Comparator<StatsInfo> {
	@Override
	public int compare(StatsInfo m, StatsInfo n) {
		return (int) (n.stats.getInfo().getTotalTime() - m.stats.getInfo().getTotalTime());
	}
}