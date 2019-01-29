package fr.iolabs.leaf.files;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeafFileRepository extends MongoRepository<LeafFileModel, String> {

}
