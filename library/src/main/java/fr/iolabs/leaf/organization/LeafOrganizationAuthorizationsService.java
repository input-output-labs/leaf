package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.UnauthorizedException;
import fr.iolabs.leaf.organization.model.LeafOrganization;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LeafOrganizationAuthorizationsService {
	@Resource(name = "coreContext")
	private LeafContext coreContext;

	public void checkIsOrganizationMember(LeafOrganization organization) {
		if (!OrganizationHelper.isMemberOfOrganization(organization, this.coreContext.getAccount().getId())) {
			throw new UnauthorizedException();
		}
	}
}