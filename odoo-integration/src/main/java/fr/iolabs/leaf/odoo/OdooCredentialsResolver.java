package fr.iolabs.leaf.odoo;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.odoo.config.LeafOdooConfig;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OdooCredentialsResolver {

	@Autowired
	private LeafModuleService leafModuleService;

	@Autowired
	private LeafOrganizationRepository leafOrganizationRepository;

	@Autowired(required = false)
	private LeafOdooConfig leafOdooConfig;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	public OdooCredentials fromModule(OdooModule module) {
		return OdooCredentials.fromModule(module);
	}

	public OdooCredentials fromGlobalConfig() {
		if (leafOdooConfig == null || !leafOdooConfig.isConfigured()) {
			throw new OdooIntegrationException("Missing global Odoo configuration (url, db, username or password)");
		}
		return new OdooCredentials(
			leafOdooConfig.getUrl(),
			leafOdooConfig.getDb(),
			leafOdooConfig.getUsername(),
			leafOdooConfig.getPassword()
		);
	}

	/**
	 * Resolves credentials from the application configuration ({@code odoo.yml} / {@code application.yml}).
	 */
	public OdooCredentials resolveFromApplicationConfig() {
		return this.fromGlobalConfig();
	}

	/**
	 * Resolves credentials from the organization's {@link OdooModule} only.
	 * Does not fall back to global configuration.
	 */
	public OdooCredentials resolveFromOrganization(ILeafModular organization) {
		OdooModule module = this.leafModuleService.get(OdooModule.class, organization, false);
		return OdooCredentials.fromModule(module);
	}

	public OdooCredentials resolveFromOrganizationId(String organizationId) {
		LeafOrganization organization =
			this.leafOrganizationRepository.findById(organizationId)
				.orElseThrow(() -> new NotFoundException("Organization not found: " + organizationId));
		return this.resolveFromOrganization(organization);
	}

	public OdooCredentials resolveFromCurrentOrganization() {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new OdooIntegrationException("No organization selected");
		}
		return this.resolveFromOrganization(organization);
	}

	/**
	 * Resolves credentials for API calls: current organization Odoo module when enabled, otherwise global configuration.
	 */
	public OdooCredentials resolveForApi() {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization != null) {
			OdooModule module = this.leafModuleService.get(OdooModule.class, organization, false);
			if (module != null && module.isEnabled()) {
				return OdooCredentials.fromModule(module);
			}
		}
		return this.fromGlobalConfig();
	}

	/**
	 * Resolves credentials from the organization module when enabled, otherwise from global configuration.
	 */
	public OdooCredentials resolve(ILeafModular organization) {
		OdooModule module = this.leafModuleService.get(OdooModule.class, organization, false);
		if (module != null && module.isEnabled()) {
			return OdooCredentials.fromModule(module);
		}
		return this.fromGlobalConfig();
	}
}
