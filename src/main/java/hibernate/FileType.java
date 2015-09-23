package hibernate;

public class FileType {
	private int id;
	private String code;
	
	public FileType() {
	}
	public FileType(String code) {
		super();
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
}