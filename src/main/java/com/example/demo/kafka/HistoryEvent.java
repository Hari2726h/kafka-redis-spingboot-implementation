package com.example.demo.kafka;

public class HistoryEvent {

	private String objectType;

	private Long objectId;

	private String action;

	public HistoryEvent() {
	}

	public HistoryEvent(String objectType, Long objectId, String action) {
		this.objectType = objectType;
		this.objectId = objectId;
		this.action = action;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}