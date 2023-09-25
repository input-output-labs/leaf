package fr.iolabs.leafdemo.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;

@Component
public class LeafAccountOrganizationMigration implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafAccountRepository accountRepository;

	public void onApplicationEvent(final ApplicationReadyEvent event) {
		List<LeafOrganization> allOrganizations = this.organizationRepository.findAll();
		for (LeafOrganization organization : allOrganizations) {
			if (this.shoudMigrate(organization)) {
				if (organization.getInvitations() == null) {
					organization.setInvitations(new ArrayList<>());
				}
				if (organization.getMembers() == null) {
					List<LeafAccount> existingMembers = accountRepository.findByOrganizationId(organization.getId());
					organization.setMembers(existingMembers.stream().map(account -> {
						OrganizationMembership organizationMembership = new OrganizationMembership();
						organizationMembership.getMetadata();
						organizationMembership.setAccountId(account.getId());
						organizationMembership.setRole("member");
						return organizationMembership;
					}).collect(Collectors.toList()));
				}
				System.out.println("Migrating organization " + organization.getId());
				this.organizationRepository.save(organization);
			}
		}
	}

	private boolean shoudMigrate(LeafOrganization organization) {
		return organization.getMembers() == null || organization.getInvitations() == null;
	}
}