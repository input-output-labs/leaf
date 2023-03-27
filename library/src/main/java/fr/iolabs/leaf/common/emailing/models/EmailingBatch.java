package fr.iolabs.leaf.common.emailing.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;

public class EmailingBatch {

	@Id
	public String id;
	private BatchCreationAction input;
	private boolean finished;
	private int nextPageIndex;
	private int maxPagesCount;
	private Map<Integer, Set<String>> sendingResults;

	public static EmailingBatch from(BatchCreationAction input) {
		EmailingBatch batch = new EmailingBatch();
		batch.input = input;
		batch.input.setTestingEmailTarget(null);
		batch.nextPageIndex = 0;
		batch.finished = false;
		batch.sendingResults = new HashMap<>();
		return batch;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String categoryName() {
		return this.input.getTarget().getName();
	}

	public boolean isCustomCategory() {
		return this.input.getTarget().getCustom();
	}

	public void checkFinished() {
		if (this.nextPageIndex >= this.maxPagesCount) {
			this.finished = true;
		}
	}

	public BatchCreationAction getInput() {
		return input;
	}

	public void setInput(BatchCreationAction input) {
		this.input = input;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public int getNextPageIndex() {
		return nextPageIndex;
	}

	public void setNextPageIndex(int nextPageIndex) {
		this.nextPageIndex = nextPageIndex;
	}

	public void incrementNextPageIndex() {
		this.nextPageIndex++;
	}

	public int getMaxPagesCount() {
		return maxPagesCount;
	}

	public void setMaxPagesCount(int maxPagesCount) {
		this.maxPagesCount = maxPagesCount;

		for (int i = 0; i < this.maxPagesCount; i++) {
			this.sendingResults.put(i, new HashSet<>());
		}
	}

	public Map<Integer, Set<String>> getSendingResults() {
		return sendingResults;
	}

	public void setSendingResults(Map<Integer, Set<String>> sendingResults) {
		this.sendingResults = sendingResults;
	}

	public void setFailedMail(String email) {
		if(this.sendingResults.get(this.nextPageIndex) == null) {
			this.sendingResults.put(this.nextPageIndex, new HashSet<>());
		}
		this.sendingResults.get(this.nextPageIndex).add(email);
	}
}
