package org.mdacc.rists.bdi.vo;

public class ConsumerFileReqVO {
	int rowId;
	int fileTypeId;
	String fileType;
	int consumerId;
	String consumer;
	
	public ConsumerFileReqVO() {
		super();
	}

	public ConsumerFileReqVO(int rowId, int fileTypeId, String fileType, int consumerId, String consumer) {
		super();
		this.rowId = rowId;
		this.fileTypeId = fileTypeId;
		this.fileType = fileType;
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
