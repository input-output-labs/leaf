package fr.iolabs.leaf.organization;

import fr.iolabs.leaf.organization.model.LeafOrganization;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeafOrganizationRepository extends MongoRepository<LeafOrganization, String> {
}