package fr.iolabs.leaf.authentication;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.authentication.model.LeafAccount;

@Repository
public interface LeafAccountRepository<T extends LeafAccount> extends MongoRepository<T, String> {
    public T findAccountByEmail(String email);

    public T findAccountByResetPasswordKey(String resetPasswordKey);
}