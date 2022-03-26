package fr.iolabs.leaf.statistics;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEvent;

public class LeafStatisticGatheringEvent extends ApplicationEvent {
	private static final long serialVersionUID = 3339748867382226670L;

	private StatisticsGatherer gatherer;
	private LocalDateTime lastGathering;

	public LeafStatisticGatheringEvent(Object source, StatisticsGatherer gatherer, LocalDateTime lastGathering) {
		super(source);
		this.gatherer = gatherer;
		this.lastGathering = lastGathering;
	}

	public StatisticsGatherer gatherer() {
		return gatherer;
	}

	public LocalDateTime getLastGathering() {
		return lastGathering;
	}
}
