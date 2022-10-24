package fr.iolabs.leaf.common.emailing.models;

public class BatchCreationTestingReport {
	private BatchCreationAction input;
	private boolean targetOk = false;
	private boolean titleOk = false;
	private boolean emailsPerHourOk = false;
	private boolean sendgridIdOk = false;
	private boolean testingEmailTargetOk = false;
	private long targetAccountsCount = -1;
	
	public BatchCreationTestingReport(BatchCreationAction input) {
		this.input = input;
	}
	
	public boolean isTestingEmailTargetOk() {
		return testingEmailTargetOk;
	}

	public void setTestingEmailTargetOk(boolean testingEmailTargetOk) {
		this.testingEmailTargetOk = testingEmailTargetOk;
	}

	public boolean canTest() {
		return this.testingEmailTargetOk && this.titleOk;
	}

	public BatchCreationAction getInput() {
		return input;
	}

	public void setInput(BatchCreationAction input) {
		this.input = input;
	}

	public boolean isTargetOk() {
		return targetOk;
	}

	public void setTargetOk(boolean targetOk) {
		this.targetOk = targetOk;
	}

	public boolean isTitleOk() {
		return titleOk;
	}

	public void setTitleOk(boolean titleOk) {
		this.titleOk = titleOk;
	}

	public boolean isEmailsPerHourOk() {
		return emailsPerHourOk;
	}

	public void setEmailsPerHourOk(boolean emailsPerHourOk) {
		this.emailsPerHourOk = emailsPerHourOk;
	}

	public boolean isSendgridIdOk() {
		return sendgridIdOk;
	}

	public void setSendgridIdOk(boolean sendgridIdOk) {
		this.sendgridIdOk = sendgridIdOk;
	}

	public long getTargetAccountsCount() {
		return targetAccountsCount;
	}

	public void setTargetAccountsCount(long targetAccountsCount) {
		this.targetAccountsCount = targetAccountsCount;
	}
}
