package fr.iolabs.leaf;

import java.util.HashMap;
import java.util.Map;

import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.organization.model.LeafOrganization;

public class LeafContext {
	private static final String ACCOUNT_KEY = "_leaf_account";
	private static final String ORGANIZATION_KEY = "_leaf_organization";
	
	private Map<String, Object> data = new HashMap<>();

    public LeafAccount getAccount() {
        return (LeafAccount) this.data.get(ACCOUNT_KEY);
    }

    public void setAccount(LeafAccount account) {
        this.data.put(ACCOUNT_KEY, account);
    }

	public LeafOrganization getOrganization() {
        return (LeafOrganization) this.data.get(ORGANIZATION_KEY);
	}

	public void setOrganization(LeafOrganization organization) {
        this.data.put(ORGANIZATION_KEY, organization);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz, String key) {
		Object value = this.data.get(key);
		return value != null && clazz.isInstance(value) ? (T) value : null;
	}

	public void set(String key, Object value) {
		this.data.put(key, value);
	}
}