package fr.iolabs.leaf.common.emailing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.emailing.models.BatchCreationAction;
import fr.iolabs.leaf.common.emailing.models.BatchCreationTestingReport;
import fr.iolabs.leaf.common.emailing.models.EmailingBatch;
import fr.iolabs.leaf.common.emailing.models.LeafEmailingCategory;
import fr.iolabs.leaf.common.errors.NotFoundException;
import fr.iolabs.leaf.common.errors.UnauthorizedException;

@RestController
@RequestMapping("/api/emailing")
public class EmailingController {
	private static final int BATCH_MIN_EMAILS_PER_HOUR = 1;
	private static final int BATCH_MAX_EMAILS_PER_HOUR = 100;

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private EmailingCategoryRepository emailingCategoryRepository;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private EmailingBatchRepository batchRepository;

	@Autowired
	private LeafSendgridEmailService sendgridEmailService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@CrossOrigin
	@AdminOnly
	@GetMapping("/categories")
	public List<LeafEmailingCategory> findAllCategories() {
		List<LeafEmailingCategory> categories = new ArrayList<LeafEmailingCategory>();

		// Get app custom categories
		this.applicationEventPublisher.publishEvent(new LeafEmailingCustomCategorySeekingEvent(this, categories));
		categories.forEach(category -> category.setCustom(true));

		// List all existing categories
		categories.addAll(this.emailingCategoryRepository.findAll());

		return categories;
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping("/categories/{id}")
	private LeafEmailingCategory findCategoryById(String id) {
		if ("0".equals(id)) {
			LeafEmailingCategory adminCategory = new LeafEmailingCategory();
			adminCategory.setId("0");
			adminCategory.setName("Admins");
			return adminCategory;
		}
		Optional<LeafEmailingCategory> optCategory = this.emailingCategoryRepository.findById(id);
		if (optCategory.isEmpty()) {
			throw new NotFoundException("No emailing category found with id " + id);
		}
		return optCategory.get();
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/categories")
	public LeafEmailingCategory createCategory(@RequestBody LeafEmailingCategory newCategory) {
		return this.emailingCategoryRepository.insert(newCategory);
	}

	@CrossOrigin
	@AdminOnly
	@PutMapping("/categories/{id}")
	public LeafEmailingCategory updateCategory(@PathVariable String id, @RequestBody LeafEmailingCategory newCategory) {
		if ("0".equals(id)) {
			throw new UnauthorizedException("Cannot update admin category");
		}
		LeafEmailingCategory existingCategory = this.findCategoryById(id);
		existingCategory.setName(newCategory.getName());
		existingCategory.setDescription(newCategory.getDescription());
		return this.emailingCategoryRepository.save(existingCategory);
	}

	@CrossOrigin
	@AdminOnly
	@DeleteMapping("/categories/{id}")
	public LeafEmailingCategory deleteCategory(@PathVariable String id) {
		if ("0".equals(id)) {
			throw new UnauthorizedException("Cannot delete admin category");
		}
		LeafEmailingCategory existingCategory = this.findCategoryById(id);
		this.emailingCategoryRepository.delete(existingCategory);
		return existingCategory;
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping("/batch")
	public List<LeafEmailingCategory> findAllBatches() {
		List<LeafEmailingCategory> categories = new ArrayList<LeafEmailingCategory>();
		LeafEmailingCategory adminCategory = new LeafEmailingCategory();
		adminCategory.setId("0");
		adminCategory.setName("Admins");
		categories.add(adminCategory);
		categories.addAll(this.emailingCategoryRepository.findAll());
		return categories;
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/batch/test")
	public BatchCreationTestingReport testEmailBatch(@RequestBody BatchCreationAction batchCreationAction) {
		BatchCreationTestingReport report = generateReportObject(batchCreationAction);

		if (report.canTest()) {
			String result = this.sendgridEmailService.sendEmailWithTemplate(batchCreationAction.getTestingEmailTarget(),
					batchCreationAction.getSengridId(), report.getInput().getTarget().getName());
			report.setSendgridIdOk(!result.contains("\"field\":\"template_id\""));
			if (result.contains("\"field\":\"to.email\"")) {
				report.setTestingEmailTargetOk(false);
			}
		}

		return report;
	}

	@CrossOrigin
	@AdminOnly
	@PostMapping("/batch")
	public EmailingBatch createEmailBatch(@RequestBody BatchCreationAction batchCreationAction) {
		BatchCreationTestingReport report = generateReportObject(batchCreationAction);

		if (!report.canTest()) {
			throw new UnauthorizedException(
					"Cannot create batch with given input. Use '/batch/test' to understand the rejection");
		}

		EmailingBatch batch = EmailingBatch.from(batchCreationAction);

		long targetedAccountsCount = 0;
		if (batch.isCustomCategory()) {
			LeafEmailingCustomCategoryAccountListingEvent event = new LeafEmailingCustomCategoryAccountListingEvent(
					this, batch.getInput().getTarget(), LeafEmailingCustomCategoryAccountListingEvent.Action.COUNT);
			this.applicationEventPublisher.publishEvent(event);
			targetedAccountsCount = event.getCount();
		} else {
			targetedAccountsCount = report.getTargetAccountsCount();
		}
		batch.setMaxPagesCount((int) Math.ceil(targetedAccountsCount / report.getInput().getEmailsPerHour()));

		return this.batchRepository.insert(batch);
	}

	private BatchCreationTestingReport generateReportObject(BatchCreationAction batchCreationAction) {
		BatchCreationTestingReport report = new BatchCreationTestingReport(batchCreationAction);
		if (batchCreationAction.getTestingEmailTarget() != null
				&& !batchCreationAction.getTestingEmailTarget().isBlank()) {
			report.setTestingEmailTargetOk(true);
		}
		if (batchCreationAction.getSengridId() != null && !batchCreationAction.getSengridId().isBlank()) {
			report.setTestingEmailTargetOk(true);
		}
		if (batchCreationAction.getSengridId() != null && !batchCreationAction.getSengridId().isBlank()) {
			report.setSendgridIdOk(true);
		}
		if (batchCreationAction.getTitle() != null && !batchCreationAction.getTitle().isBlank()) {
			report.setTitleOk(true);
		}
		if (batchCreationAction.getEmailsPerHour() >= BATCH_MIN_EMAILS_PER_HOUR
				&& batchCreationAction.getEmailsPerHour() <= BATCH_MAX_EMAILS_PER_HOUR) {
			report.setEmailsPerHourOk(true);
		}
		if (batchCreationAction.getTarget() != null) {
			try {
				LeafEmailingCategory target = this.findCategoryById(batchCreationAction.getTarget().getId());
				if (target != null) {
					report.setTargetOk(true);
					report.setTargetAccountsCount(this.targetAccountsCount(target));
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		}
		return report;
	}

	private long targetAccountsCount(LeafEmailingCategory target) {
		if ("0".equals(target.id)) {
			// Admin list
			return this.accountRepository.countByAdminTrue();
		}
		return this.accountRepository.countAccountsSubscribedTo(target.getName().toLowerCase());
	}

}
