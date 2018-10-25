package fr.iolabs.leaf.admin.whitelisting;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhitelistingRepository extends MongoRepository<AuthorizedEmail, String> {

}
