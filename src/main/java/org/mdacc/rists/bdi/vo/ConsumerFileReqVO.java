package org.mdacc.rists.bdi.vo;

public class ConsumerFileReqVO {
	int rowId;
	int fileTypeId;
	String fileType;
	String apiUri;
	String apiUsername;
	String apiPassword;
	int consumerId;
	String consumer;
	
	public ConsumerFileReqVO() {
		super();
	}

	public ConsumerFileReqVO(int rowId, int fileTypeId, String fileType, String uri, 
			String username, String password, int consumerId, String consumer) {
		super();
		this.rowId = rowId;
		this.fileTypeId = fileTypeId;
		this.fileType = fileType;
		this.apiUri = uri;
		this.apiUsername = username;
		this.apiPassword = password;
		this.consumerId = consumerId;
		this.consumer = consumer;
	}

	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public int getFileTypeId() {
		return fileTypeId;
	}
	public void setFileTypeId(int fileTypeId) {
		this.fileTypeId = fileTypeId;
	}
	
	public String getApiUri() {
		return apiUri;
	}

	public void setApiUri(String apiUri) {
		this.apiUri = apiUri;
	}

	public String getApiUsername() {
		return apiUsername;
	}

	public void setApiUsername(String apiUsername) {
		this.apiUsername = apiUsername;
	}

	public String getApiPassword() {
		return apiPassword;
	}

	public void setApiPassword(String apiPassword) {
		this.apiPassword = apiPassword;
	}

	public int getConsumerId() {
		return consumerId;
	}
	public void setConsumerId(int consumerId) {
		this.consumerId = consumerId;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getConsumer() {
		return consumer;
	}

	public void setConsumer(String consumer) {
		this.consumer = consumer;
	}
	

}
