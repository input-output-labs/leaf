package fr.iolabs.leaf.eligibilities;

import java.util.Map;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eligibilities")
public class LeafEligibilitiesController {

	@Autowired
	private LeafEligibilitiesService eligibilitiesService;

	@PermitAll
	@CrossOrigin
	@GetMapping
	public Map<String, LeafEligibility> create() {
		return this.eligibilitiesService.getEligibilities();
	}
}
