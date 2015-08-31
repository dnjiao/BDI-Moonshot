package imt_data;

public class FlowGate {
	String name;
	String code;
	String definition;
	String parent;
	double value;
	int col;
	
	public FlowGate(String name, String code, String definition, String parent,	int col) {
		super();
		this.name = name;
		this.code = code;
		this.definition = definition;
		this.parent = parent;
		this.col = col;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}
	
	
}