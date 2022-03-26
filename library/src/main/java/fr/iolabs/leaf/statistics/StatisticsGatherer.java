package fr.iolabs.leaf.statistics;

public interface StatisticsGatherer {
	public void postStatistic(String key, Long value);
}
