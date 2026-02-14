package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.model.LeafOrganization;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafOrganizationRepository extends MongoRepository<LeafOrganization, String> {

	@Query("{ 'name': { $regex: ?0, $options: 'i' } }")
	List<LeafOrganization> findByNameRegex(String nameRegex);

	@Query("{ 'name': { $regex: ?0, $options: 'i' } }")
	List<LeafOrganization> findByNameRegex(String nameRegex, Pageable pageable);
}