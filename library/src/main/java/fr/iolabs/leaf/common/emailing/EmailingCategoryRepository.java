package fr.iolabs.leaf.common.emailing;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.common.emailing.models.LeafEmailingCategory;

@Repository
public interface EmailingCategoryRepository extends MongoRepository<LeafEmailingCategory, String> {
	public Optional<LeafEmailingCategory> findByName(String name);
}
