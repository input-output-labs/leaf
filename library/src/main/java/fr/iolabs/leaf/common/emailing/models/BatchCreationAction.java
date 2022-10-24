package fr.iolabs.leaf.common.emailing.models;

public class BatchCreationAction {
	private LeafEmailingCategory target;
	private String title;
	private String sengridId;
	private int emailsPerHour;
	private String testingEmailTarget;

	public LeafEmailingCategory getTarget() {
		return target;
	}

	public void setTarget(LeafEmailingCategory target) {
		this.target = target;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSengridId() {
		return sengridId;
	}

	public void setSengridId(String sengridId) {
		this.sengridId = sengridId;
	}

	public int getEmailsPerHour() {
		return emailsPerHour;
	}

	public void setEmailsPerHour(int emailsPerHour) {
		this.emailsPerHour = emailsPerHour;
	}

	public String getTestingEmailTarget() {
		return testingEmailTarget;
	}

	public void setTestingEmailTarget(String testingEmailTarget) {
		this.testingEmailTarget = testingEmailTarget;
	}

}
