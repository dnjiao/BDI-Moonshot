package hibernate;

public class FileType {
	private int id;
	private String code;
	
	public FileType(String code) {
		super();
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
}