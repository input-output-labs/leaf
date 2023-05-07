package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafOrganizationRepository extends MongoRepository<LeafOrganization, String> {
}