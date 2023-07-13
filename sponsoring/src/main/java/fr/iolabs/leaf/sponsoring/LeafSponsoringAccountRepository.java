package fr.iolabs.leaf.sponsoring;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.authentication.model.LeafAccount;

@Repository
public interface LeafSponsoringAccountRepository extends MongoRepository<LeafAccount, String> {
	@Query(value = "{'modules.sponsoring.sponsorCode': {$eq: ?0}}")
	public Optional<LeafAccount> findAccountBySponsorCode(String sponsorCode);
}