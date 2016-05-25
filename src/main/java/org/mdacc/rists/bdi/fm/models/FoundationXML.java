package org.mdacc.rists.bdi.fm.models;

public class FoundationXML {
	private String reportId;
	private String sampleId;
	private String fmId;
	private String mrn;
	private String diagnosis;
	public FoundationXML(String reportId, String sampleId, String fmId,
			String mrn, String diagnosis) {
		super();
		this.reportId = reportId;
		this.sampleId = sampleId;
		this.fmId = fmId;
		this.mrn = mrn;
		this.diagnosis = diagnosis;
	}
	public FoundationXML() {
	}
	public String getReportId() {
		return reportId;
	}
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	public String getSampleId() {
		return sampleId;
	}
	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
	public String getFmId() {
		return fmId;
	}
	public void setFmId(String fmId) {
		this.fmId = fmId;
	}
	public String getMrn() {
		return mrn;
	}
	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	public String getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}
	
	
}
