package fr.iolabs.leaf.authentication;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.authentication.model.LeafAccount;

@Repository
public interface LeafAccountRepository extends MongoRepository<LeafAccount, String> {
    public LeafAccount findAccountByEmail(String email);

    public LeafAccount findAccountByResetPasswordKey(String resetPasswordKey);
}