package fr.iolabs.leaf.payment.services;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

@Repository
public interface LeafServiceRepository extends MongoRepository<LeafService, String> {
    
    @Query("{ 'attachmentType' : ?0, 'attachedTo' : ?1 }")
    List<LeafService> findByAttachmentTypeAndAttachedTo(PlanAttachment attachmentType, String attachedTo);
    
    @Query("{ 'attachedTo' : ?0 }")
    List<LeafService> findByAttachedTo(String attachedTo);
    
    @Query("{ 'key' : ?0, 'attachmentType' : ?1, 'attachedTo' : ?2 }")
    List<LeafService> findByKeyAndAttachmentTypeAndAttachedTo(String key, PlanAttachment attachmentType, String attachedTo);
}
