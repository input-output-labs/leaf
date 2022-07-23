package fr.iolabs.leaf.analytics;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.analytics.models.LeafAnalyticEvent;

@Repository
public interface LeafAnalyticsRepository extends MongoRepository<LeafAnalyticEvent, String> {}
