package fr.iolabs.leaf.payment.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

@Service
public class LeafServiceService {

	@Autowired
	private LeafPaymentConfig paymentConfig;

    @Autowired
    private LeafServiceRepository leafServiceRepository;

    @Autowired
    private LeafServiceSubscriptionSynchronization leafServiceSubscriptionSynchronization;
    
	public List<LeafService> fetchAvailableServices() {
		return this.paymentConfig.getServices();
	}

    /**
     * Create a new service
     */
    @Transactional
    public LeafService createService(LeafService leafService) {
        if (leafService == null) {
            throw new BadRequestException("Service cannot be null");
        }
        if (leafService.getKey() == null || leafService.getKey().trim().isEmpty()) {
            throw new BadRequestException("Service key cannot be null or empty");
        }
        if (leafService.getAttachmentType() == null) {
            throw new BadRequestException("Attachment type cannot be null");
        }
        if (leafService.getAttachedTo() == null || leafService.getAttachedTo().trim().isEmpty()) {
            throw new BadRequestException("Attached to cannot be null or empty");
        }
        if (leafService.getUnitPrice() < 0) {
            throw new BadRequestException("Unit price cannot be negative");
        }
        if (leafService.getQuantity() < 0) {
            throw new BadRequestException("Quantity cannot be negative");
        }
        
        LeafService service = leafServiceRepository.save(leafService);
        this.leafServiceSubscriptionSynchronization.synchronizeServicesFor(service.getAttachmentType(), service.getAttachedTo());
        return service;
    }

    /**
     * Get a service by ID
     */
    public LeafService getServiceById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Service ID cannot be null or empty");
        }
        
        Optional<LeafService> service = leafServiceRepository.findById(id);
        if (!service.isPresent()) {
            throw new NotFoundException("Service with ID " + id + " not found");
        }
        
        return service.get();
    }

    public LeafService updateService(String id, LeafService updatedService) {
    	return this.updateService(id, updatedService, true);
    }

    /**
     * Update an existing service
     */
    @Transactional
    public LeafService updateService(String id, LeafService updatedService, boolean synchronize) {
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Service ID cannot be null or empty");
        }
        if (updatedService == null) {
            throw new BadRequestException("Updated service cannot be null");
        }

        LeafService existingService = getServiceById(id);
        
        // Update fields
        if (updatedService.getKey() != null && !updatedService.getKey().trim().isEmpty()) {
            existingService.setKey(updatedService.getKey());
        }
        if (updatedService.getIcon() != null && !updatedService.getIcon().trim().isEmpty()) {
            existingService.setIcon(updatedService.getIcon());
        }
        if (updatedService.getAttachmentType() != null) {
            existingService.setAttachmentType(updatedService.getAttachmentType());
        }
        if (updatedService.getAttachedTo() != null && !updatedService.getAttachedTo().trim().isEmpty()) {
            existingService.setAttachedTo(updatedService.getAttachedTo());
        }
        if (updatedService.getUnitPrice() >= 0) {
            existingService.setUnitPrice(updatedService.getUnitPrice());
        }
        if (updatedService.getQuantity() >= 0) {
            existingService.setQuantity(updatedService.getQuantity());
        }
        // Update new boolean fields
        if (updatedService.getMetadata() != null) {
            existingService.setMetadata(updatedService.getMetadata());
        }

        // Update last modification timestamp
        existingService.getMetadata().updateLastModification();

        LeafService service = leafServiceRepository.save(existingService);
        if (synchronize) {
            this.leafServiceSubscriptionSynchronization.synchronizeServicesFor(service.getAttachmentType(), service.getAttachedTo());
        }
        return service;
    }

    /**
     * Delete a service by ID
     */
    @Transactional
    public void deleteService(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Service ID cannot be null or empty");
        }

        LeafService service = getServiceById(id);
        leafServiceRepository.delete(service);
        this.leafServiceSubscriptionSynchronization.synchronizeServicesFor(service.getAttachmentType(), service.getAttachedTo());
    }

    /**
     * Get all services for a specific attachment (account or organization)
     */
    public List<LeafService> getServicesByAttachment(PlanAttachment attachmentType, String attachedTo) {
        if (attachmentType == null) {
            throw new BadRequestException("Attachment type cannot be null");
        }
        if (attachedTo == null || attachedTo.trim().isEmpty()) {
            throw new BadRequestException("Attached to cannot be null or empty");
        }

        return leafServiceRepository.findByAttachmentTypeAndAttachedTo(attachmentType, attachedTo);
    }

    /**
     * Get all services for a specific entity (regardless of attachment type)
     */
    public List<LeafService> getServicesByAttachedTo(String attachedTo) {
        if (attachedTo == null || attachedTo.trim().isEmpty()) {
            throw new BadRequestException("Attached to cannot be null or empty");
        }

        return leafServiceRepository.findByAttachedTo(attachedTo);
    }

    /**
     * Get services by key and attachment
     */
    public List<LeafService> getServicesByKeyAndAttachment(String key, PlanAttachment attachmentType, String attachedTo) {
        if (key == null || key.trim().isEmpty()) {
            throw new BadRequestException("Service key cannot be null or empty");
        }
        if (attachmentType == null) {
            throw new BadRequestException("Attachment type cannot be null");
        }
        if (attachedTo == null || attachedTo.trim().isEmpty()) {
            throw new BadRequestException("Attached to cannot be null or empty");
        }

        return leafServiceRepository.findByKeyAndAttachmentTypeAndAttachedTo(key, attachmentType, attachedTo);
    }

    /**
     * Get all services
     */
    public List<LeafService> getAllServices() {
        return leafServiceRepository.findAll();
    }
}
