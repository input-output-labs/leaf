package fr.iolabs.leaf.common.emailing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.emailing.models.EmailingBatch;

@Service
public class EmailingService {

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private EmailingBatchRepository batchRepository;

	@Autowired
	private LeafSendgridEmailService sendgridEmailService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Scheduled(cron = "0 0 * * * *")
	public void scheduleFixedRateTask() {
		List<EmailingBatch> batches = this.batchRepository.findByFinishedFalse();
		for (EmailingBatch batch : batches) {
			this.sendBatchEmails(batch);

			batch.incrementNextPageIndex();
			batch.checkFinished();
			this.batchRepository.save(batch);
		}

		if (batches.isEmpty()) {
			System.out.println("No pending emailing batch");
		}
	}

	private void sendBatchEmails(EmailingBatch batch) {
		System.out.println("Found emailing batch : " + batch.getId());
		double batchSize = batch.getInput().getEmailsPerHour();
		String categoryName = batch.categoryName();

		int pageIndex = batch.getNextPageIndex();

		Pageable page = PageRequest.of(pageIndex, (int) batchSize);

		List<LeafAccount> accounts;
		if (batch.isCustomCategory()) {
			LeafEmailingCustomCategoryAccountListingEvent event = new LeafEmailingCustomCategoryAccountListingEvent(
					this,
					batch.getInput().getTarget() ,LeafEmailingCustomCategoryAccountListingEvent.Action.LIST, page);
			this.applicationEventPublisher.publishEvent(event);
			accounts = event.accounts();
		} else {
			accounts = this.accountRepository.listAccountsSubscribedTo(categoryName, page);
		}

		for (LeafAccount account : accounts) {

			try {
				String result = this.sendgridEmailService.sendEmailWithTemplate(account.getEmail(),
						batch.getInput().getSengridId(), categoryName);
				if (!result.isEmpty()) {
					batch.setFailedMail(account.getEmail());
				}
			} catch (Exception e) {
				batch.setFailedMail(account.getEmail());
				e.printStackTrace();
			}

		}
	}
}
