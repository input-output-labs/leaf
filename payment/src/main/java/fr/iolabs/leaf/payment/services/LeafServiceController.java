package fr.iolabs.leaf.payment.services;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.annotations.MandatoryOrganization;
import fr.iolabs.leaf.payment.plan.config.PlanAttachment;

@RestController
@RequestMapping("/api/payment/services")
public class LeafServiceController {

    @Resource(name = "coreContext")
    private LeafContext coreContext;

    @Autowired
    private LeafServiceService leafServiceService;

    /**
     * Create a new service
     */
    @AdminOnly
    @CrossOrigin
    @PostMapping
    public LeafService createService(@RequestBody LeafService leafService) {
        return leafServiceService.createService(leafService);
    }

    /**
     * Get a service by ID
     */
    @AdminOnly
    @CrossOrigin
    @GetMapping("/{id}")
    public LeafService getServiceById(@PathVariable String id) {
        return leafServiceService.getServiceById(id);
    }

    /**
     * Update a service
     */
    @AdminOnly
    @CrossOrigin
    @PutMapping("/{id}")
    public LeafService updateService(@PathVariable String id, @RequestBody LeafService updatedService) {
        return leafServiceService.updateService(id, updatedService);
    }

    /**
     * Delete a service
     */
    @AdminOnly
    @CrossOrigin
    @DeleteMapping("/{id}")
    public void deleteService(@PathVariable String id) {
        leafServiceService.deleteService(id);
    }

    /**
     * Get all services for a specific organization
     */
    @AdminOnly
    @CrossOrigin
    @GetMapping("/organization/{organizationId}")
    public List<LeafService> getServicesByOrganization(@PathVariable String organizationId) {
        return leafServiceService.getServicesByAttachment(PlanAttachment.ORGANIZATION, organizationId);
    }

    /**
     * Get all services for a specific account
     */
    @AdminOnly
    @CrossOrigin
    @GetMapping("/account/{accountId}")
    public List<LeafService> getServicesByAccount(@PathVariable String accountId) {
        return leafServiceService.getServicesByAttachment(PlanAttachment.USER, accountId);
    }

    /**
     * Get all services for the current user's organization
     */
    @CrossOrigin
    @MandatoryOrganization
    @GetMapping("/my-organization")
    public List<LeafService> getMyOrganizationServices() {
        return leafServiceService.getServicesByAttachment(PlanAttachment.ORGANIZATION, coreContext.getOrganization().getId());
    }

    /**
     * Get all services for the current user
     */
    @CrossOrigin
    @GetMapping("/my-account")
    public List<LeafService> getMyAccountServices() {
        return leafServiceService.getServicesByAttachment(PlanAttachment.USER, coreContext.getAccount().getId());
    }

    /**
     * Get services by key and attachment type
     */
    @AdminOnly
    @CrossOrigin
    @GetMapping("/search")
    public List<LeafService> getServicesByKeyAndAttachment(
            @RequestParam String key,
            @RequestParam PlanAttachment attachmentType,
            @RequestParam String attachedTo) {
        return leafServiceService.getServicesByKeyAndAttachment(key, attachmentType, attachedTo);
    }

    /**
     * Get all services (admin only)
     */
    @AdminOnly
    @CrossOrigin
    @GetMapping
    public List<LeafService> getAllServices() {
        return leafServiceService.getAllServices();
    }
}
