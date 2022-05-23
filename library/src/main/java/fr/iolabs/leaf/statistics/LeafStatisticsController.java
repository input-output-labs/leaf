package fr.iolabs.leaf.statistics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.common.annotations.AdminOnly;

@RestController
@RequestMapping("/api/statistics")
public class LeafStatisticsController {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafStatisticsRepository statisticsRepository;

	@CrossOrigin
	@AdminOnly
	@GetMapping
	public List<LeafStatistic> listStatistics() {
		List<LeafStatistic> statistics = this.statisticsRepository.findAll().stream()
				.sorted((h1, h2) -> h1.getCreationDateTime().compareTo(h2.getCreationDateTime()))
				.collect(Collectors.toList());
		LeafStatistic currentStatistic = this.getCurrentStatistics();
		statistics.add(currentStatistic);
		return statistics;
	}

	public Optional<LeafStatistic> lastStatistic() {
		return this.statisticsRepository.findAll().stream()
				.sorted((h1, h2) -> h1.getCreationDateTime().compareTo(h2.getCreationDateTime())).findFirst();
	}

	@Scheduled(cron = "0 30 0 * * *")
	public void reportCurrentTime() {
		LeafStatistic statistic = this.getCurrentStatistics();
		this.statisticsRepository.insert(statistic);
	}

	private LeafStatistic getCurrentStatistics() {
		LeafStatistic statistic = LeafStatistic.create();
		Optional<LeafStatistic> lastStatistic = this.lastStatistic();
		this.applicationEventPublisher.publishEvent(new LeafStatisticGatheringEvent(this, statistic,
				lastStatistic.isPresent() ? lastStatistic.get().getCreationDateTime() : null));
		return statistic;
	}
}
