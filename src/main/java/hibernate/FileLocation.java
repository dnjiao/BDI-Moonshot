package hibernate;

import org.joda.time.DateTime;

public class FileLocation {
	private int rowId;
	private int fileTypeId;
	private String dirPath;
	private DateTime lastCopy;
	private String type;
	
	
	public FileLocation() {
	}

	public FileLocation(int fileTypeId, String dirPath, DateTime lastCopy) {
		super();
		this.fileTypeId = fileTypeId;
		this.dirPath = dirPath;
		this.lastCopy = lastCopy;
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

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public DateTime getLastCopy() {
		return lastCopy;
	}

	public void setLastCopy(DateTime lastCopy) {
		this.lastCopy = lastCopy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

}