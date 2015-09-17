package hibernate;

import org.joda.time.DateTime;

public class FileLocation {
	private int rowId;
	private String dirPath;
	private String type;
	private DateTime lastCopy;
	private FileType fileType;

	public FileLocation(int rowId, String dirPath, String type,
			DateTime lastCopy, FileType fileType) {
		super();
		this.rowId = rowId;
		this.dirPath = dirPath;
		this.type = type;
		this.lastCopy = lastCopy;
		this.fileType = fileType;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public String getDirPath() {
		return dirPath;
	}

	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DateTime getLastCopy() {
		return lastCopy;
	}

	public void setLastCopy(DateTime lastCopy) {
		this.lastCopy = lastCopy;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
	
}