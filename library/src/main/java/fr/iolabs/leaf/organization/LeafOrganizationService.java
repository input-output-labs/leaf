package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.ResourceMetadata;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.organization.actions.CreateOrganizationAction;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;
import fr.iolabs.leaf.organization.policies.LeafOrganizationPoliciesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.util.Strings;

import javax.annotation.Resource;

@Service
public class LeafOrganizationService {
	@Resource(name = "coreContext")
	private LeafContext coreContext;
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafOrganizationPoliciesService policiesService;

	public List<LeafOrganization> listAll() {
		return organizationRepository.findAll();
	}

	public List<LeafOrganization> listAll(String nameFilter, Integer limit) {
		String escapedFilter = nameFilter != null ? Pattern.quote(nameFilter) : null;
		if (escapedFilter != null && limit != null) {
			return organizationRepository.findByNameRegex(escapedFilter, PageRequest.of(0, limit));
		} else if (escapedFilter != null) {
			return organizationRepository.findByNameRegex(escapedFilter);
		} else if (limit != null) {
			return organizationRepository.findAll(PageRequest.of(0, limit)).getContent();
		}
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
		organization.setPolicies(this.policiesService.createDefaultPolicies());

		OrganizationMembership firstMember = new OrganizationMembership();
		firstMember.setAccountId(coreContext.getAccount().getId());
		firstMember.setRole(this.policiesService.extractCreatorDefaultRole(organization.getPolicies()));
		firstMember.getMetadata();

		organization.getMembers().add(firstMember);

		this.applicationEventPublisher.publishEvent(new OrganizationCreationEvent(this, organization));
		LeafOrganization savedOrganization = organizationRepository.save(organization);
		this.applicationEventPublisher.publishEvent(new OrganizationCreatedEvent(this, organization));

		coreContext.getAccount().getOrganizationIds().add(savedOrganization.getId());
		this.accountRepository.save(coreContext.getAccount());

		return savedOrganization;
	}

	public LeafOrganization updateProfile(String organizationId, LeafAccountProfile profile) {
		LeafOrganization organization = this.getById(organizationId).orElseThrow(NotFoundException::new);
		if (organization.getProfile() == null) {
			organization.setProfile(profile);
		} else {
			organization.getProfile().updateWith(profile);
		}
		if (profile != null && profile.getDisplayName() != null) {
			organization.setName(profile.getDisplayName());
		}

		this.applicationEventPublisher.publishEvent(new OrganizationProfileUpdateEvent(this, organization));

		return this.organizationRepository.save(organization);
	}

	public boolean isMemberOfOrganization() {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new InternalServerErrorException("Cannot invoke this function without @MandatoryOrganization");
		}
		return this.isMemberOfOrganization(organization);
	}

	public boolean isMemberOfOrganization(LeafOrganization organization) {
		LeafAccount account = this.coreContext.getAccount();
		if (!account.getOrganizationIds().contains(organization.getId())) {
			return false;
		}
		for(OrganizationMembership member : organization.getMembers()) {
			if (account.getId().equals(member.getAccountId())) {
				return true;
			}
		}
        return false;
	}

	public OrganizationSearchResponse search(OrganizationSearchCriteria criteria) {
		if (criteria == null) {
			criteria = new OrganizationSearchCriteria();
		}

		Query countQuery = new Query();
		if (Strings.isNotBlank(criteria.getName())) {
			countQuery.addCriteria(Criteria.where("name").regex(Pattern.quote(criteria.getName().trim()), "i"));
		}

		long totalCount = this.mongoTemplate.count(countQuery, LeafOrganization.class);

		Query pageQuery = new Query();
		if (Strings.isNotBlank(criteria.getName())) {
			pageQuery.addCriteria(Criteria.where("name").regex(Pattern.quote(criteria.getName().trim()), "i"));
		}
		pageQuery.with(PageRequest.of(criteria.getPage(), criteria.getPageSize(), this.resolveSort(criteria.getOrderBy())));

		List<LeafOrganization> organizations = this.mongoTemplate.find(pageQuery, LeafOrganization.class);
		int pageCount = (int) Math.ceil((double) totalCount / criteria.getPageSize());

		return new OrganizationSearchResponse(organizations, totalCount, pageCount, criteria.getPage());
	}

	private Sort resolveSort(OrganizationSearchOrder orderBy) {
		OrganizationSearchOrder order = orderBy != null ? orderBy : OrganizationSearchOrder.NAME;
		switch (order) {
		case CREATION_DATE:
			return Sort.by(Sort.Direction.DESC, "metadata.creationDate");
		case NAME:
		default:
			return Sort.by(Sort.Direction.ASC, "name");
		}
	}
}