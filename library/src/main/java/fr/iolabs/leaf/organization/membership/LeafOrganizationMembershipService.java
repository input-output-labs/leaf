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
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationInvitation;
import fr.iolabs.leaf.organization.model.OrganizationInvitationStatus;
import fr.iolabs.leaf.organization.model.OrganizationMembership;
import fr.iolabs.leaf.organization.model.dto.OrganizationInvitationData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	@Value("${leaf.protocol_hostname}")
	String hostUrl;

	@Value("${leaf.emailing.sendgrid.templates.organization-invitation-template}")
	String sendgridOrganizationInvitationTemplateId;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Transactional
	public void addUsersToOrganization(String organizationId, Set<String> accountIds) {
		Iterable<LeafAccount> accounts = accountRepository.findAllById(accountIds);
		LeafOrganization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));

		accounts.forEach((account) -> {
			for (OrganizationMembership member : organization.getMembers()) {
				if (account.getId().equals(member.getAccountId())) {
					// Already a member of the organization, skipping ...
					return;
				}
			}
			account.getOrganizationIds().add(organization.getId());

			OrganizationMembership newMember = new OrganizationMembership();
			newMember.setAccountId(account.getId());
			newMember.setRole("member");
			newMember.getMetadata();
			organization.getMembers().add(newMember);
		});

		organizationRepository.save(organization);
		accountRepository.saveAll(accounts);
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

	@Transactional
	public LeafOrganization inviteUserToOrganization(String organizationId, String email) {
		if (email == null || email.isBlank()) {
			throw new BadRequestException("Email must not be blank");
		}
		LeafOrganization organization = this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));

		// Check if already invited
		for (OrganizationInvitation invitation : organization.getInvitations()) {
			if (email.equals(invitation.getEmail())) {
				throw new BadRequestException("This user has already been invited to the organization");
			}
		}

		OrganizationInvitation invitation = new OrganizationInvitation();
		invitation.setStatus(OrganizationInvitationStatus.INVITED);
		invitation.setEmail(email);
		invitation.getMetadata();

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
		newMember.setRole("member");
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
	public void cancelInvitation(String organizationId, String email) {
		LeafOrganization organization = this.organizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found"));
		OrganizationInvitation invitation = this.findInvitation(organization, email);
		if (invitation.getStatus() != OrganizationInvitationStatus.INVITED) {
			throw new BadRequestException("This invitation cannot be accepted anymore.");
		}
		invitation.setAccountId(coreContext.getAccount().getId());
		invitation.setStatus(OrganizationInvitationStatus.CANCELLED);
		invitation.getMetadata().updateLastModification();

		this.organizationRepository.save(organization);
	}
}