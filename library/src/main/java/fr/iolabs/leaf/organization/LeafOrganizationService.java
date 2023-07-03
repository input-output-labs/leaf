package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LeafOrganizationService {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafAccountRepository accountRepository;

	public List<LeafOrganization> listAll() {
		return organizationRepository.findAll();
	}

	public Optional<LeafOrganization> getById(String id) {
		return organizationRepository.findById(id);
	}

	public Iterable<LeafOrganization> getByIds(Set<String> organizationIds) {
		if (organizationIds != null && organizationIds.size() > 0) {
			return this.organizationRepository.findAllById(organizationIds);
		}
		return List.of();
	}

	public LeafOrganization create(CreateOrganizationAction action) {
		LeafOrganization organization = new LeafOrganization();
		organization.setName(action.getName());
		organization.setMetadata(ResourceMetadata.create());

		this.applicationEventPublisher.publishEvent(new OrganizationCreationEvent(this, organization));

		return organizationRepository.save(organization);
	}

	@Transactional
	public void addUsersToOrganization(String organizationId, Set<String> accountIds) {
		Iterable<LeafAccount> accounts = accountRepository.findAllById(accountIds);
		LeafOrganization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));

		accounts.forEach((account) -> account.getOrganizationIds().add(organization.getId()));

		accountRepository.saveAll(accounts);
	}

	public List<LeafAccount> listUsers(String organizationId) {
		return accountRepository.findByOrganizationId(organizationId);
	}
}