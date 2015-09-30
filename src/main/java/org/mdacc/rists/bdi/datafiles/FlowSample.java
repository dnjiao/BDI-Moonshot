package org.mdacc.rists.bdi.datafiles;

import java.util.List;

public class FlowSample {
	String mrn;
	String specimenID;
	String protocol;
	String date;
	String cycle;
	int accession;
	List<FlowGate> gates;
	
	public FlowSample(String mrn, String specimenID, String date, List<FlowGate> gates) {
		super();
		this.mrn = mrn;
		this.specimenID = specimenID;
		this.date = date;
		this.gates = gates;
	}

	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

	public String getSpecimenID() {
		return specimenID;
	}

	public void setSpecimenID(String specimenID) {
		this.specimenID = specimenID;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public int getAccession() {
		return accession;
	}

	public void setAccession(int accession) {
		this.accession = accession;
	}

	public List<FlowGate> getGates() {
		return gates;
	}

	public void setGates(List<FlowGate> gates) {
		this.gates = gates;
	}
	
	
}