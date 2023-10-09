package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.organization.model.OrganizationMembership;

public class OrganizationHelper {
	public static boolean isMemberOfOrganization(LeafOrganization organization, String accountId) {
		for (OrganizationMembership member : organization.getMembers()) {
			if (accountId.equals(member.getAccountId())) {
				return true;
			}
		}
		return false;
	}
}
