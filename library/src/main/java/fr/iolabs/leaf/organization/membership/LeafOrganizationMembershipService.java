package fr.iolabs.leaf.organization.membership;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.emailing.LeafSendgridEmailService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.notifications.LeafNotification;
import fr.iolabs.leaf.notifications.LeafNotificationService;
import fr.iolabs.leaf.organization.LeafOrganizationAuthorizationsService;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.OrganizationHelper;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationInvitation;
import fr.iolabs.leaf.organization.model.OrganizationInvitationStatus;
import fr.iolabs.leaf.organization.model.OrganizationMembership;
import fr.iolabs.leaf.organization.model.OrganizationRole;
import fr.iolabs.leaf.organization.model.candidature.OrganizationCandidature;
import fr.iolabs.leaf.organization.model.candidature.OrganizationCandidatureStatus;
import fr.iolabs.leaf.organization.model.config.LeafOrganizationConfig;
import fr.iolabs.leaf.organization.model.dto.OrganizationCandidatureData;
import fr.iolabs.leaf.organization.model.dto.OrganizationCandidatureData.OrganizationCandidatureDataError;
import fr.iolabs.leaf.organization.model.dto.OrganizationInvitationData;
import fr.iolabs.leaf.organization.policies.LeafOrganizationPoliciesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.annotation.Resource;

@Service
public class LeafOrganizationMembershipService {
	@Autowired
	private LeafOrganizationRepository organizationRepository;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafSendgridEmailService leafSendgridEmailService;

	@Autowired
	private LeafNotificationService leafNotificationService;

	@Value("${leaf.appDomain}")
	String hostUrl;

	@Value("${leaf.emailing.sendgrid.templates.organization-invitation-template}")
	String sendgridOrganizationInvitationTemplateId;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafOrganizationPoliciesService policiesService;

	@Autowired
	private LeafOrganizationAuthorizationsService organizationAuthorizationsService;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private LeafOrganizationConfig organizationConfig;

	@Transactional
	public void addUsersToOrganization(String organizationId, Set<String> accountIds) {
		Iterable<LeafAccount> accounts = accountRepository.findAllById(accountIds);
		LeafOrganization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));

		accounts.forEach((account) -> {
			if (OrganizationHelper.isMemberOfOrganization(organization, account.getId())) {
				// Already a member of the organization, skipping ...
				return;
			}
			account.getOrganizationIds().add(organization.getId());

			OrganizationMembership newMember = new OrganizationMembership();
			newMember.setAccountId(account.getId());
			newMember.setRole(this.policiesService.extractOtherDefaultRole(organization.getPolicies()));
			newMember.getMetadata();
			organization.getMembers().add(newMember);
		});

		organizationRepository.save(organization);
		accountRepository.saveAll(accounts);
	}

	@Transactional
	public LeafOrganization removeUserFromOrganization(String accountId) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);
		LeafAccount account = accountRepository.findById(accountId)
				.orElseThrow(() -> new NotFoundException("Account not found"));

		if (!OrganizationHelper.isMemberOfOrganization(organization, accountId)) {
			throw new NotFoundException("This user is not a member of this organization");
		}

		organization.setMembers(organization.getMembers().stream()
				.filter(member -> !member.getAccountId().equals(account.getId())).collect(Collectors.toList()));
		
		int adminCount = 0;
		String creatorRole = organizationConfig.getCreatorDefaultName();
		for (OrganizationMembership member : organization.getMembers()) {
			String memberRole = member.getRole();
			if (memberRole != null && memberRole.equals(creatorRole)) {
				adminCount++;
			}
		}
		if (adminCount == 0) {
			throw new BadRequestException("Cannot remove last " + creatorRole + " of the organization");
		}
		
		account.getOrganizationIds().remove(organization.getId());

		accountRepository.save(account);
		return organizationRepository.save(organization);
	}

	public LeafOrganization setUserRole(String accountId, String role) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);
		accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Account not found"));

		if (!OrganizationHelper.isMemberOfOrganization(organization, accountId)) {
			throw new NotFoundException("This user is not a member of this organization");
		}

		organization.getMembers().forEach(member -> {
			if (member.getAccountId().equals(accountId)) {
				member.setRole(role);
			}
		});

		return organizationRepository.save(organization);
	}

	public List<LeafAccount> listUsers(String organizationId) {
		LeafOrganization organization = organizationRepository.findById(organizationId)
				.orElseThrow(NotFoundException::new);
		Set<String> organizationMembersId = organization.getMembers().stream().map(member -> member.getAccountId())
				.collect(Collectors.toSet());
		List<LeafAccount> accounts = accountRepository.findByOrganizationId(organizationId);
		return accounts.stream().filter(account -> organizationMembersId.contains(account.getId()))
				.collect(Collectors.toList());
	}
	
	public LeafOrganization inviteUserToOrganization(String email) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		return this.inviteUserToOrganization(organization, email);
	}

	@Transactional
	public LeafOrganization inviteUserToOrganization(LeafOrganization organization, String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be blank");
		}
		if (organization == null) {
			throw new NotFoundException("Organization must not be null");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);

		// Check if already invited
		OrganizationInvitation existingInvitation = null;
		for (OrganizationInvitation invitation : organization.getInvitations()) {
			if (email.equals(invitation.getEmail())) {
				existingInvitation = invitation;
			}
		}

		if (existingInvitation != null && existingInvitation.getStatus() == OrganizationInvitationStatus.INVITED) {
			throw new BadRequestException("This user has already been invited to the organization");
		}

		OrganizationInvitation invitation = existingInvitation != null ? existingInvitation
				: new OrganizationInvitation();
		if (existingInvitation == null) {
			invitation.setEmail(email);
		}
		invitation.setStatus(OrganizationInvitationStatus.INVITED);
		invitation.getMetadata().updateLastModification();

		LeafAccount existingAccount = this.accountRepository.findAccountByEmail(email);
		if (existingAccount != null) {
			// Check if already a member
			boolean alreadyAMember = existingAccount.getOrganizationIds().contains(organization.getId());
			for (OrganizationMembership member : organization.getMembers()) {
				if (existingAccount.getId().equals(member.getAccountId())) {
					alreadyAMember = false;
				}
			}
			if (alreadyAMember) {
				throw new BadRequestException("This user is already part of the organization");
			}
			invitation.setAccountId(existingAccount.getId());
		}

		Map<String, Object> invitationNotificationPayload = new HashMap<String, Object>();
		invitationNotificationPayload.put("hostUrl", this.hostUrl);
		invitationNotificationPayload.put("senderName", this.generateSenderName());
		invitationNotificationPayload.put("organizationId", organization.getId());
		invitationNotificationPayload.put("organizationName", organization.getName());
		invitationNotificationPayload.put("invitationEmail", email);

		if (existingAccount != null) {
			// Send notification
			this.leafNotificationService.emit(LeafNotification.of("LEAF_ORGANIZATION_INVITATION",
					existingAccount.getId(), invitationNotificationPayload));
		} else {
			// Send email directly
			this.leafSendgridEmailService.sendEmailWithTemplate(email, this.sendgridOrganizationInvitationTemplateId,
					invitationNotificationPayload);
		}
		organization.getInvitations().add(invitation);
		return this.organizationRepository.save(organization);
	}

	private String generateSenderName() {
		LeafAccountProfile profile = this.coreContext.getAccount().getProfile();
		if (profile.getFirstname() != null || profile.getLastname() != null) {
			StringJoiner sj = new StringJoiner(" ");
			if (profile.getFirstname() != null) {
				sj.add(profile.getFirstname());
			}
			if (profile.getLastname() != null) {
				sj.add(profile.getLastname());
			}
			return sj.toString();
		} else if (profile.getUsername() != null) {
			return profile.getUsername();
		}
		return this.coreContext.getAccount().getEmail();
	}

	public OrganizationInvitation findInvitation(LeafOrganization organization, String email) {
		for (OrganizationInvitation invitation : organization.getInvitations()) {
			if (email.equals(invitation.getEmail())) {
				return invitation;
			}
		}
		throw new NotFoundException("No invitation found");
	}

	public OrganizationCandidature findCandidature(LeafOrganization organization, String email) {
		for (OrganizationCandidature candidature : organization.getCandidatureManagement().getCandidatures()) {
			if (OrganizationCandidatureStatus.CANDIDATED == candidature.getStatus() && email.equalsIgnoreCase(candidature.getEmail())) {
				return candidature;
			}
		}
		throw new NotFoundException("No candidature found");
	}

	public OrganizationInvitationData getInvitationData(String organizationId, String email) {
		LeafOrganization organization = this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));
		OrganizationInvitation invitation = this.findInvitation(organization, email);
		OrganizationInvitationData invitationData = new OrganizationInvitationData();
		invitationData.setInvitation(invitation);
		invitationData.setName(organization.getName());
		return invitationData;
	}

	@Transactional
	public void acceptInvitation(String organizationId, String email) {
		LeafOrganization organization = this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));

		for (OrganizationMembership member : organization.getMembers()) {
			if (this.coreContext.getAccount().getId().equals(member.getAccountId())) {
				throw new BadRequestException("Already a member of this organization.");
			}
		}

		OrganizationInvitation invitation = this.findInvitation(organization, email);
		if (invitation.getStatus() != OrganizationInvitationStatus.INVITED) {
			throw new BadRequestException("This invitation cannot be accepted anymore.");
		}
		invitation.setAccountId(this.coreContext.getAccount().getId());
		invitation.setStatus(OrganizationInvitationStatus.ACCEPTED);
		invitation.getMetadata().updateLastModification();

		OrganizationMembership newMember = new OrganizationMembership();
		newMember.setAccountId(coreContext.getAccount().getId());
		newMember.setRole(this.policiesService.extractOtherDefaultRole(organization.getPolicies()));
		newMember.getMetadata();

		organization.getMembers().add(newMember);

		this.coreContext.getAccount().getOrganizationIds().add(organization.getId());
		this.accountRepository.save(this.coreContext.getAccount());

		this.organizationRepository.save(organization);
	}

	@Transactional
	public void declineInvitation(String organizationId, String email) {
		LeafOrganization organization = this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));
		OrganizationInvitation invitation = this.findInvitation(organization, email);
		if (invitation.getStatus() != OrganizationInvitationStatus.INVITED) {
			throw new BadRequestException("This invitation cannot be accepted anymore.");
		}
		invitation.setAccountId(coreContext.getAccount().getId());
		invitation.setStatus(OrganizationInvitationStatus.DECLINED);
		invitation.getMetadata().updateLastModification();

		this.organizationRepository.save(organization);
	}

	@Transactional
	public void cancelInvitation(String email) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);
		OrganizationInvitation invitation = this.findInvitation(organization, email);
		if (invitation.getStatus() != OrganizationInvitationStatus.INVITED) {
			throw new BadRequestException("This invitation cannot be accepted anymore.");
		}
		invitation.setAccountId(coreContext.getAccount().getId());
		invitation.setStatus(OrganizationInvitationStatus.CANCELLED);
		invitation.getMetadata().updateLastModification();

		this.organizationRepository.save(organization);
	}

	public LeafOrganization enableCandidatureManagement(boolean enable) {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new NotFoundException("Organization must be provided in Organization header");
		}
		this.organizationAuthorizationsService.checkIsOrganizationMember(organization);
		organization.getCandidatureManagement().setEnabled(enable);

		this.applicationEventPublisher.publishEvent(new LeafOrganizationCandidatureEnableEvent(this, organization, enable));
		return this.organizationRepository.save(organization);
	}

	public OrganizationCandidatureData getOrganizationCandidatureData(String organizationId, String role) {
		OrganizationCandidatureData data = new OrganizationCandidatureData();
		Optional<LeafOrganization> optOrganization = this.organizationRepository.findById(organizationId);
		if (optOrganization.isEmpty()) {
			data.setError(OrganizationCandidatureDataError.MISSING_ORGANIZATION);
			return data;
		}
		LeafOrganization organization = optOrganization.get();
		data.setOrganizationName(organization.getName());
		if (!organization.getCandidatureManagement().isEnabled()) {
			data.setError(OrganizationCandidatureDataError.CANDIDATURE_DISABLED);
			return data;
		}
		String existingRoleName = null;
		for (OrganizationRole existingRole :organization.getPolicies().getRoles()) {
			if (existingRole.getName().equalsIgnoreCase(role)) {
				existingRoleName = existingRole.getName();
			}
		}
		if (existingRoleName == null) {
			data.setError(OrganizationCandidatureDataError.INVALID_ROLE);
			return data;
		}
		
		LeafAccount me = this.coreContext.getAccount();
		for(OrganizationMembership member : organization.getMembers()) {
			if (me.getId().equals(member.getAccountId())) {
				data.setError(OrganizationCandidatureDataError.ALREADY_MEMBER_OF_ORGANIZATION);
				return data;
			}
		}

		return data;
	}

	public void candidateToOrganization(String organizationId, String role) {
		LeafAccount me = this.coreContext.getAccount();
		Optional<LeafOrganization> optOrganization = this.organizationRepository.findById(organizationId);
		if (optOrganization.isEmpty()) {
			throw new NotFoundException("No organization found with id " + organizationId);
		}
		LeafOrganization organization = optOrganization.get();
		if (!organization.getCandidatureManagement().isEnabled()) {
			throw new BadRequestException("Cannot candidate to this organization");
		}
		for (OrganizationMembership member : organization.getMembers()) {
			if (member.getAccountId().equals(me.getId())) {
				throw new BadRequestException("Already a member of the organization");
			}
		}
		String existingRoleName = null;
		for (OrganizationRole existingRole :organization.getPolicies().getRoles()) {
			if (existingRole.getName().equalsIgnoreCase(role)) {
				existingRoleName = existingRole.getName();
			}
		}
		if (existingRoleName == null) {
			throw new BadRequestException("Requested role does not exists");
		}
		
		OrganizationCandidature candidature = null;
		for (OrganizationCandidature existingCandidature : organization.getCandidatureManagement().getCandidatures()) {
			boolean validStatus = OrganizationCandidatureStatus.CANDIDATED == existingCandidature.getStatus();
			boolean sameEmail = me.getEmail().equals(existingCandidature.getEmail());
			if (validStatus && sameEmail) {
				candidature = existingCandidature;
			}
		}
		
		if (candidature == null) {
			candidature = new OrganizationCandidature();
			candidature.setAccountId(me.getId());
			candidature.setEmail(me.getEmail());
			candidature.setStatus(OrganizationCandidatureStatus.CANDIDATED);
			organization.getCandidatureManagement().getCandidatures().add(candidature);
		}
		candidature.setRole(existingRoleName);
		candidature.getMetadata().updateLastModification();
		this.organizationRepository.save(organization);
		
		String creatorDefault = "admin";
		for(OrganizationRole orgRole : organization.getPolicies().getRoles()) {
			if (orgRole.isCreatorDefault()) {
				creatorDefault = orgRole.getName();
			}
		}
		final String filterByRole = creatorDefault;
		List<OrganizationMembership> membersToNotify = organization.getMembers().stream().filter(member -> {
			return filterByRole != null && filterByRole.equalsIgnoreCase(member.getRole());
		}).toList();
		
		Map<String, Object> notificationPayload = new HashMap<>();
		notificationPayload.put("organizationId", organizationId);
		notificationPayload.put("organizationName", organization.getName());
		notificationPayload.put("candidateEmail", me.getEmail());
		
		for (OrganizationMembership memberToNotify: membersToNotify) {
			this.leafNotificationService.emit(LeafNotification.of("LEAF_ORGANIZATION_CANDIDATURE_RECEIVED", memberToNotify.getAccountId(), notificationPayload));
		}
	}

	@Transactional
	public void acceptCandidature(String email) {
		LeafOrganization organization = this.coreContext.getOrganization();
		
		if (!organization.getCandidatureManagement().isEnabled()) {
			throw new BadRequestException("Organization does not accept candidatures.");
		}


		OrganizationCandidature candidature = this.findCandidature(organization, email);
		if (candidature.getStatus() != OrganizationCandidatureStatus.CANDIDATED) {
			throw new BadRequestException("This invitation cannot be accepted anymore.");
		}

		Optional<LeafAccount> optCandidateAccount = this.accountRepository.findById(candidature.getAccountId());
		if (optCandidateAccount.isEmpty()) {
			throw new BadRequestException("Candidate account does not exists anymore.");
		}
		LeafAccount candidateAccount = optCandidateAccount.get();

		for (OrganizationMembership member : organization.getMembers()) {
			if (member.getAccountId().equals(candidature.getAccountId())) {
				throw new BadRequestException("Already a member of this organization.");
			}
		}
		candidature.setStatus(OrganizationCandidatureStatus.ACCEPTED);
		candidature.getMetadata().updateLastModification();

		OrganizationMembership newMember = new OrganizationMembership();
		newMember.setAccountId(candidature.getAccountId());
		newMember.setRole(candidature.getRole());
		newMember.getMetadata();

		organization.getMembers().add(newMember);

		
		candidateAccount.getOrganizationIds().add(organization.getId());
		this.accountRepository.save(candidateAccount);

		this.organizationRepository.save(organization);
	}

	@Transactional
	public void declineCandidature(String email) {
		LeafOrganization organization = this.coreContext.getOrganization();
		OrganizationCandidature candidature = this.findCandidature(organization, email);
		if (candidature.getStatus() != OrganizationCandidatureStatus.CANDIDATED) {
			throw new BadRequestException("This invitation cannot be accepted anymore.");
		}
		candidature.setStatus(OrganizationCandidatureStatus.DECLINED);
		candidature.getMetadata().updateLastModification();

		this.organizationRepository.save(organization);
	}
}
