package fr.iolabs.leaf.statistics;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class LeafStatistic implements StatisticsGatherer {
	@Id
	private String id;
	private LocalDateTime creationDateTime;
	private Map<String, Long> data;

	public static LeafStatistic create() {
		LeafStatistic created = new LeafStatistic();
		created.setCreationDateTime(LocalDateTime.now());
		created.setData(new HashMap<>());
		return created;
	}

	@Override
	public void postStatistic(String key, Long value) {
		this.data.put(key, value);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getCreationDateTime() {
		return creationDateTime;
	}

	public void setCreationDateTime(LocalDateTime creationDateTime) {
		this.creationDateTime = creationDateTime;
	}

	public Map<String, Long> getData() {
		return data;
	}

	public void setData(Map<String, Long> data) {
		this.data = data;
	}

}
