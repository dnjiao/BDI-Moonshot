package org.mdacc.rists.bdi.vo;

public class FileQueueVO {
	int rowId;
	String fileUri;
	String fileName;
	
	public FileQueueVO() {
		super();
	}
	public FileQueueVO(int rowId, String fileUri) {
		super();
		this.rowId = rowId;
		this.fileUri = fileUri;
	}

	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public String getFileUri() {
		return fileUri;
	}
	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

}
