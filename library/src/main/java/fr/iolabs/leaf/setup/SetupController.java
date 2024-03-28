package fr.iolabs.leaf.setup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.authentication.read.LeafAccountReadController;
import fr.iolabs.leaf.eligibilities.LeafEligibilitiesController;
import fr.iolabs.leaf.notifications.LeafNotificationController;
import fr.iolabs.leaf.organization.LeafOrganizationController;

@RestController
@RequestMapping("/api/setup")
public class SetupController {

	@Autowired
	private LeafAccountReadController leafAccountReadController;

	@Autowired
	private LeafNotificationController leafNotificationController;

	@Autowired
	private LeafOrganizationController leafOrganizationController;

	@Autowired
	private LeafEligibilitiesController leafEligibilitiesController;

	@CrossOrigin
	@GetMapping
	public SetupResponse fetchInitData(
			@RequestParam(value = "user", required = false, defaultValue = "true") boolean user,
			@RequestParam(value = "notifications", required = false, defaultValue = "false") boolean notifications,
			@RequestParam(value = "organizations", required = false, defaultValue = "false") boolean organizations,
			@RequestParam(value = "eligibilities", required = false, defaultValue = "false") boolean eligibilities) {
		SetupResponse response = new SetupResponse();

		if (user) {
			response.setUser(this.leafAccountReadController.getUser());
		}

		if (notifications) {
			response.setNotifications(this.leafNotificationController.listMyNotifications());
		}

		if (organizations) {
			response.setOrganizations(this.leafOrganizationController.listMyOrganizations());
		}

		if (eligibilities) {
			response.setEligibilities(this.leafEligibilitiesController.list());
		}

		return response;
	}
}
