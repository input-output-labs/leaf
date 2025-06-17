package fr.iolabs.leaf.eligibilities;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEvent;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.organization.model.LeafOrganization;

public class LeafEligibilitiesEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3508017274339153100L;

	private Map<String, LeafEligibility> eligibilities;
	private List<String> eligibilityKeys;
	private LeafAccount account;
	private LeafOrganization organization;

	public LeafEligibilitiesEvent(Object source, Map<String, LeafEligibility> eligibilities, LeafAccount account, LeafOrganization organization) {
		this(source, eligibilities, account, organization, null);
	}

	public LeafEligibilitiesEvent(Object source, Map<String, LeafEligibility> eligibilities, LeafAccount account, LeafOrganization organization, List<String> eligibilityKeys) {
		super(source);
		this.eligibilities = eligibilities;
		this.eligibilityKeys = eligibilityKeys;
		this.account = account;
		this.organization = organization;
	}

	public Map<String, LeafEligibility> eligibilities() {
		return this.eligibilities;
	}

	public List<String> eligibilityKey() {
		return this.eligibilityKeys;
	}

	public LeafOrganization getOrganization() {
		return this.organization;
	}

	public LeafAccount getAccount() {
		return this.account;
	}
}