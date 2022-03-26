package fr.iolabs.leaf.statistics;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafStatisticsRepository extends MongoRepository<LeafStatistic, String> {}
