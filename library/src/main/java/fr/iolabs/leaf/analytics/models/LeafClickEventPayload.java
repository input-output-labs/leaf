package fr.iolabs.leaf.analytics.models;

public class LeafClickEventPayload {
    private String eventType;
    private String clickType;
    private String clickTargetId;
    private String clickTargetText;
    private String currentPageId;
    private String currentPageTitle;
    /**
     * Click action
     */
    private String action;

    public LeafClickEventPayload() {
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getClickType() {
        return clickType;
    }

    public void setClickType(String clickType) {
        this.clickType = clickType;
    }

    public String getClickTargetId() {
        return clickTargetId;
    }

    public void setClickTargetId(String clickTargetId) {
        this.clickTargetId = clickTargetId;
    }

    public String getClickTargetText() {
        return clickTargetText;
    }

    public void setClickTargetText(String clickTargetText) {
        this.clickTargetText = clickTargetText;
    }

    public String getCurrentPageId() {
        return currentPageId;
    }

    public void setCurrentPageId(String currentPageId) {
        this.currentPageId = currentPageId;
    }

    public String getCurrentPageTitle() {
        return currentPageTitle;
    }

    public void setCurrentPageTitle(String currentPageTitle) {
        this.currentPageTitle = currentPageTitle;
    }

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
