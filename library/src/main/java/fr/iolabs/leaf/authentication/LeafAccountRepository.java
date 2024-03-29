package fr.iolabs.leaf.authentication;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.authentication.model.LeafAccount;

@Repository
public interface LeafAccountRepository extends MongoRepository<LeafAccount, String> {
	public LeafAccount findAccountByEmail(String email);

	@Query(value = "{'authentication.resetPasswordKey': {$eq: ?0}}")
	public LeafAccount findAccountByResetPasswordKey(String resetPasswordKey);

	public List<LeafAccount> findByAdminTrue();

	public List<LeafAccount> findByAdminTrue(Pageable pageable);

	@Query("{ 'organizationIds' : { $eq: ?0 } }")
	public List<LeafAccount> findByOrganizationId(String organizationId);

	@Query("{ 'organizationIds' : { $eq: ?0 } }")
	public List<LeafAccount> findByOrganizationId(String organizationId, Pageable pageable);

	public long countByAdminTrue();

	@Query(value = "{'communication.unsubscription': {$not: {$in: [?0]}}}", count = true)
	public long countAccountsSubscribedTo(String name);

	@Query(value = "{'communication.unsubscription': {$not: {$in: [?0]}}}", fields = "{ 'email': 1}")
	public List<LeafAccount> listAccountsSubscribedTo(String name, Pageable pageable);

	@Query(value = "{'profile.username': {'$regex': ?0, '$options': 'i'}}")
	public List<LeafAccount> findByUsernameLike(String input, PageRequest pageRequest);

	public Iterable<LeafAccount> findAllById(Iterable<String> ids);
}