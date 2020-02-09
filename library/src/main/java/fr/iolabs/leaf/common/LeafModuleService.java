package fr.iolabs.leaf.common;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;

@Service
public class LeafModuleService {

    @Resource(name = "coreContext")
    private LeafContext coreContext;
    
    public <T> T get(Class<T> clazz) {
    	LeafAccount account = this.coreContext.getAccount();
    	if (account == null) {
    		throw new InternalServerErrorException("LeafModuleService - get() cannot be used if no user is logged in");
    	}
    	return this.get(clazz, account);
    }
    
    public <T> T get(Class<T> clazz, LeafAccount account) {
    	String className = clazz.getSimpleName();
    	Object module = account.getModules().get(className);
		return module != null && clazz.isInstance(module)? (T) module : this.instanciate(clazz);
    }

	private <T> T instanciate(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
    		throw new InternalServerErrorException("LeafModuleService - " + clazz.getName() + " does not have default constructor");
		}
	}
}
