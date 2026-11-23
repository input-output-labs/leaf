package fr.iolabs.leaf.setup;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.authentication.read.LeafAccountReadController;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.eligibilities.LeafEligibilitiesController;
import fr.iolabs.leaf.notifications.LeafNotificationController;
import fr.iolabs.leaf.organization.LeafOrganizationController;
import fr.iolabs.leaf.organization.model.LeafOrganization;

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
			@RequestParam(value = "eligibilities", required = false, defaultValue = "false") boolean eligibilities,
			@RequestHeader(value = "Organization", required = false) String organizationId) {
		SetupResponse response = new SetupResponse();

		if (user) {
			response.setUser(this.leafAccountReadController.getUser());
		}

		if (notifications) {
			response.setNotifications(this.leafNotificationController.listMyNotifications());
		}

		if (organizations) {
			response.setOrganizations(this.organizationsForSetup(organizationId));
		}

		if (eligibilities) {
			response.setEligibilities(this.leafEligibilitiesController.list());
		}

		return response;
	}

	private List<LeafOrganization> organizationsForSetup(String organizationId) {
		List<LeafOrganization> organizations = new ArrayList<>();
		this.leafOrganizationController.listMyOrganizations().forEach(organizations::add);
		String headerOrganizationId = organizationId == null ? "" : organizationId.trim();
		if (headerOrganizationId.isEmpty()) {
			return organizations;
		}
		boolean alreadyListed = organizations.stream()
			.anyMatch(organization -> organization != null && headerOrganizationId.equals(organization.getId()));
		if (alreadyListed) {
			return organizations;
		}
		try {
			LeafOrganization headerOrganization = this.leafOrganizationController.getOrganizationById(headerOrganizationId);
			if (headerOrganization != null) {
				organizations.add(headerOrganization);
			}
		} catch (NotFoundException exception) {
			return organizations;
		}
		return organizations;
	}
}
