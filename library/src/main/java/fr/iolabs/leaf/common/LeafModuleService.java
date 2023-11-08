package fr.iolabs.leaf.common;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;

@Service
public class LeafModuleService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	public <T> T get(Class<T> clazz) {
		return this.getForAccount(clazz);
	}

	public <T> T getForAccount(Class<T> clazz) {
		ILeafModular account = this.coreContext.getAccount();
		if (account == null) {
			throw new InternalServerErrorException("LeafModuleService - get() cannot be used if no user is logged in");
		}
		return this.get(clazz, account);
	}

	public <T> T getForOrganization(Class<T> clazz) {
		ILeafModular organization = this.coreContext.getOrganization();
		if (organization == null) {
			throw new InternalServerErrorException("LeafModuleService - get() cannot be used if no organization is selected");
		}
		return this.get(clazz, organization);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz, ILeafModular account) {
		Object module = account.getModules().get(this.getModuleKey(clazz));
		return module != null && clazz.isInstance(module) ? (T) module : this.instanciate(account, clazz);
	}

	private <T> T instanciate(ILeafModular account, Class<T> clazz) {
		try {
			T module = clazz.getDeclaredConstructor().newInstance();
			account.getModules().put(this.getModuleKey(clazz), module);
			return module;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new InternalServerErrorException(
					"LeafModuleService - " + clazz.getName() + " does not have default constructor");
		}
	}

	@SuppressWarnings("rawtypes")
	private String getModuleKey(Class clazz) {
		return clazz.getSimpleName().toLowerCase();
	}
}
