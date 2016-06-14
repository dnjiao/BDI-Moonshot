package org.mdacc.rists.bdi.db.models;

public class FileQueueResult {
	int rowId;
	String fileUri;
	String fileName;
	
	public FileQueueResult() {
		super();
	}
	public FileQueueResult(int rowId, String fileUri) {
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
