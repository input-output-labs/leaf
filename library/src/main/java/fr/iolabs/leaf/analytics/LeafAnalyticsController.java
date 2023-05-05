package fr.iolabs.leaf.analytics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@CrossOrigin
	@PermitAll
	@GetMapping()
	public List<LeafAnalyticEvent> getAnalyticsByAccountId(
			@RequestParam(value = "accountId", required = false) String accountId) {
		List<LeafAnalyticEvent> events = this.analyticsRepository.findAll();
		List<LeafAnalyticEvent> filteredEvents = events.stream().filter(event -> event.getAccountId().equals(accountId)).collect(Collectors.toList());
		return filteredEvents;
	}
}