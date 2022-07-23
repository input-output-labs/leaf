package fr.iolabs.leaf.analytics;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.analytics.models.LeafAnalyticEvent;

@RestController
@RequestMapping("/api/analytics")
public class LeafAnalyticsController {

	@Autowired
	private LeafAnalyticsRepository analyticsRepository;

	@CrossOrigin
	@PermitAll
	@PostMapping()
	public void insertAnalytics(@RequestBody List<LeafAnalyticEvent> events) {
		this.analyticsRepository
				.saveAll(events.stream().filter(LeafAnalyticEvent::isValid).collect(Collectors.toList()));
	}
}
