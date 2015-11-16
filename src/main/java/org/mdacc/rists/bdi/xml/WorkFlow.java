package org.mdacc.rists.bdi.xml;

import java.util.ArrayList;
import java.util.List;

public class WorkFlow {
	String devEnv;
	String type;
	List<String> sources;
	
	
	public String getDevEnv() {
		return devEnv;
	}


	public void setDevEnv(String devEnv) {
		this.devEnv = devEnv;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public List<String> getSources() {
		return sources;
	}


	public void setSources(List<String> sources) {
		this.sources = sources;
	}


	public WorkFlow() {
		super();
		sources = new ArrayList<String>();
	}
	
}