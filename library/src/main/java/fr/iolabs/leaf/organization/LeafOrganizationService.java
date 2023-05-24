package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.LeafAccountService;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.actions.AddUserToOrganizationAction;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

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


	public LeafOrganization create(CreateOrganizationAction action) {
		LeafOrganization organization = new LeafOrganization();
		organization.setName(action.getName());
		organization.setMetadata(ResourceMetadata.create());

		this.applicationEventPublisher.publishEvent(new OrganizationCreationEvent(this, organization));

		return organizationRepository.save(organization);
	}

	@Transactional
	public void addUserToOrganization(String organizationId, String accountId) {
		LeafAccount account = accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("User not found"));
		LeafOrganization organization = organizationRepository.findById(organizationId).orElseThrow(() -> new NotFoundException("Organization not found"));

		account.getOrganizationIds().add(organization.getId());
		accountRepository.save(account);
	}

	public List<LeafAccount> listUsers(String organizationId) {
		return accountRepository.findByOrganizationId(organizationId);
	}
}