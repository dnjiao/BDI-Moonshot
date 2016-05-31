package org.mdacc.rists.bdi.fm.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mdacc.rists.bdi.fm.dao.SpecimenDao;
import org.mdacc.rists.bdi.fm.models.FmReportAltPropertyTb;
import org.mdacc.rists.bdi.fm.models.FmReportAltTb;
import org.mdacc.rists.bdi.fm.models.FmReportAltTherapyTb;
import org.mdacc.rists.bdi.fm.models.FmReportAltTrialLkTb;
import org.mdacc.rists.bdi.fm.models.FmReportAmendmendTb;
import org.mdacc.rists.bdi.fm.models.FmReportAppTb;
import org.mdacc.rists.bdi.fm.models.FmReportGeneTb;
import org.mdacc.rists.bdi.fm.models.FmReportPertNegTb;
import org.mdacc.rists.bdi.fm.models.FmReportReferenceTb;
import org.mdacc.rists.bdi.fm.models.FmReportSampleTb;
import org.mdacc.rists.bdi.fm.models.FmReportSignatureTb;
import org.mdacc.rists.bdi.fm.models.FmReportTb;
import org.mdacc.rists.bdi.fm.models.FmReportTrialTb;
import org.mdacc.rists.bdi.fm.models.FmReportVarPropetyTb;
import org.mdacc.rists.bdi.fm.models.FmReportVarTb;
import org.mdacc.rists.bdi.fm.models.SpecimenTb;
import org.mdacc.rists.bdi.utils.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class InsertFmReport {
	public static void main (String args[]) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(args[0]);
		insertReportTb(file);
	}
	
	public static void insertReportTb (File file) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			// get etl_proc_id from sequence and set to all tables
			EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("RIStore_Flow");
			EntityManager em = emFactory.createEntityManager();
			Query q = em.createNativeQuery("SELECT ETL_PROC_SEQ.nextval from DUAL");
			BigDecimal etlProcId=(BigDecimal)q.getSingleResult();
			// get current datetime for insert_ts and update_ts
			Date date = new Date();
			
			builder = dbFactory.newDocumentBuilder();
			Document doc;
			doc = builder.parse(file);
			SpecimenTb specimenTb = new SpecimenTb();
			FmReportTb report = new FmReportTb();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			NodeList childNodes = doc.getDocumentElement().getChildNodes();
			//FinalReport begins
			Node finalReport = XMLParser.getNode("FinalReport", childNodes);
			NodeList frNodes = finalReport.getChildNodes();
			report.setFrReportId(XMLParser.getNodeValue("ReportId", frNodes));
			report.setFrSampleName(XMLParser.getNodeValue("SampleName", frNodes));
			String version = XMLParser.getNodeValue("Version", frNodes);
			if (version != "") {
				report.setFrVersion(new BigDecimal(version));
			}
			
			//xsd
			Node demoCorrectionDate = XMLParser.getNode("DemographicCorrectionDate", frNodes);
			if (demoCorrectionDate != null) {
				String correctionDate = XMLParser.getNodeValue("CorrectionDate", demoCorrectionDate.getChildNodes());
				if (correctionDate != "") {
					report.setFrCorrectionDate(formatter.parse(correctionDate));
				}
			}
			
			// Sample node
			Node sample = XMLParser.getNode("Sample", frNodes);
			NodeList sampNodes = sample.getChildNodes();
			report.setFrFmId(XMLParser.getNodeValue("FM_Id", sampNodes));
			report.setFrSampleId(XMLParser.getNodeValue("SampleId", sampNodes));
			String blockId = XMLParser.getNodeValue("BlockId", sampNodes);
			report.setFrBlockId(blockId);
			//block_id as the specimen_no in specimen_tb
			specimenTb.setSpecimenNo(blockId);
			report.setFrTfrNumber(XMLParser.getNodeValue("TRFNumber", sampNodes));
			report.setFrSpecFormat(XMLParser.getNodeValue("SpecFormat", sampNodes));
			String receivedDate = XMLParser.getNodeValue("ReceivedDate", sampNodes);
			if (receivedDate != "") {
				report.setFrReceiveDate(formatter.parse(receivedDate));
			}
			
			//PMI node
			Node pmi = XMLParser.getNode("PMI", frNodes);
			NodeList pmiNodes = pmi.getChildNodes();
			// set MRN in specimen_tb
			specimenTb.setMrn(XMLParser.getNodeValue("MRN", pmiNodes));
			
			report.setFrFullName(XMLParser.getNodeValue("FullName", pmiNodes));
			report.setFrFirstName(XMLParser.getNodeValue("FirstName", pmiNodes));
			report.setFrLastName(XMLParser.getNodeValue("LastName", pmiNodes));
			report.setFrDiagnosis(XMLParser.getNodeValue("SubmittedDiagnosis", pmiNodes));
			report.setFrGender(XMLParser.getNodeValue("Gender", pmiNodes));
			String dob = XMLParser.getNodeValue("DOB", pmiNodes);
			if (dob != ""){
				report.setFrDob(formatter.parse(dob));
			}
			report.setFrOrderingMd(XMLParser.getNodeValue("OrderingMD", pmiNodes));
			report.setFrOrderingMdId(XMLParser.getNodeValue("OrderingMDId", pmiNodes));
			report.setFrPathologist(XMLParser.getNodeValue("Pathologist", pmiNodes));
			report.setFrCopiedPhysician(XMLParser.getNodeValue("CopiedPhysician1", pmiNodes));
			report.setFrFacilityName(XMLParser.getNodeValue("MedFacilName", pmiNodes));
			report.setFrFacilityId(XMLParser.getNodeValue("MedFacilID", pmiNodes));
			report.setFrSpecSite(XMLParser.getNodeValue("SpecSite", pmiNodes));
			String collDate = XMLParser.getNodeValue("CollDate", pmiNodes);
			if (collDate != "") {
				report.setFrCollectionDate(formatter.parse(collDate));
			}
			
			// Summaries node
			Node summaries = XMLParser.getNode("Summaries", frNodes);
			String countStr = XMLParser.getNodeAttr("alterationCount", summaries);
			if (countStr != "") {
				report.setFrAlterationCount(new BigDecimal(countStr));
			}
			countStr = XMLParser.getNodeAttr("clinicalTrialCount", summaries);
			if (countStr != "") {
				report.setFrTrialCount(new BigDecimal(countStr));
			}
			countStr = XMLParser.getNodeAttr("resistiveCount", summaries);
			if (countStr != "") {
				report.setFrResistiveCount(new BigDecimal(countStr));
			}
			countStr = XMLParser.getNodeAttr("sensitizingCount", summaries);
			if (countStr != "") {
				report.setFrSensitizingCount(new BigDecimal(countStr));
			}
			report.setFrSummary(XMLParser.getNodeAttr("ClinicalTrialSummary", summaries));
			
			//VariantReport begins
			Node variantReport = XMLParser.getNode("variant-Report", childNodes);
			if (variantReport != null) {
				NodeList vrNodes = variantReport.getChildNodes();
				report.setVrTissueOfOrigin(XMLParser.getNodeAttr("tissue-of-origin", variantReport));
				report.setVrTestType(XMLParser.getNodeAttr("test-type", variantReport));
				report.setVrTestRequest(XMLParser.getNodeAttr("test-request", variantReport));
				report.setVrStudy(XMLParser.getNodeAttr("study", variantReport));
				report.setVrSpecimen(XMLParser.getNodeAttr("specimen", variantReport));
				String purityAssessment = XMLParser.getNodeAttr("purity-assessment", variantReport);
				if (purityAssessment != "") {
					report.setVrPurityAssessment(new BigDecimal(purityAssessment));
				}				
				report.setVrPipelineVersion(XMLParser.getNodeAttr("pipeline-version", variantReport));
				report.setVrPercentTumorNuclei(XMLParser.getNodeAttr("percent-tumor-nuclei", variantReport));
				report.setVrPathologyDiagnosis(XMLParser.getNodeAttr("pathology-diagnosis", variantReport));
				report.setVrDiseaseOntology(XMLParser.getNodeAttr("disease-oncology", variantReport));
				report.setVrDisease(XMLParser.getNodeAttr("disease", variantReport));
				//xsd
				report.setVrStandardNucleicAcidType(XMLParser.getNodeAttr("standard-nucleic-acid-types", variantReport));
				report.setVrGender(XMLParser.getNodeAttr("gender", variantReport));
				String flowcellAnalysis = XMLParser.getNodeAttr("flowcell-analysis", variantReport);
				if (flowcellAnalysis != "") {
					report.setVrFlowcellAnalysis(new BigDecimal(flowcellAnalysis));
				}
				
				// quality-control node
				Node quality = XMLParser.getNode("quality-control", variantReport.getChildNodes());
				report.setVrQualityControlStatus(XMLParser.getNodeAttr("status", quality));
			}
		
			// ReportPDF
			Node reportPdf = XMLParser.getNode("ReportPDF", childNodes);
			report.setReportPdf(XMLParser.getNodeValue(reportPdf));
			
			report.setEtlProcId(etlProcId);
			specimenTb.setEtlProcId(etlProcId);
			
			// set insert_ts and update_ts 
			report.setInsertTs(date);
			report.setUpdateTs(date);
			specimenTb.setInsertTs(date);
			specimenTb.setUpdateTs(date);			
			specimenTb.setSpecimenSource("FM");
			
////////////////////////////////////////////////////////
			//FinalReport/Trials
			Node trials = XMLParser.getNode("Trials", frNodes);
			List<FmReportTrialTb> trialList = new ArrayList<FmReportTrialTb>();
			if (trials != null) {
				trialList = FmParseUtils.parseTrials(trials, date, etlProcId, report);
			}
			report.setFmReportTrialTbs(trialList);
			
			//FinalReport/Application/ApplicationSettings/			
			Node application = XMLParser.getNode("Application", frNodes);
			List<FmReportAppTb> appList = new ArrayList<FmReportAppTb>();
			if (application != null) {
				Node appSettings = XMLParser.getNode("ApplicationSettings", application.getChildNodes());
				if (appSettings != null) {
					appList = FmParseUtils.parseApplication(appSettings, date, etlProcId, report);
				}
			}
			report.setFmReportAppTbs(appList);
			
			//FinalReport/PertinentNegatives/
			Node pertNegs = XMLParser.getNode("PertinentNegatives", frNodes);
			List<FmReportPertNegTb> pertList = new ArrayList<FmReportPertNegTb>();
			if (pertNegs != null) {
				pertList = FmParseUtils.parsePert(pertNegs, date, etlProcId, report);
			}
			report.setFmReportPertNegTbs(pertList);
			
			//FinalReport/VariantProperties/
			Node varProp = XMLParser.getNode("VariantProperties", frNodes);
			List<FmReportVarPropetyTb> vpList = new ArrayList<FmReportVarPropetyTb>();
			if (varProp != null) {
				vpList = FmParseUtils.parseVarProperty(varProp, date, etlProcId, report);
			}
			report.setFmReportVarPropetyTbs(vpList);
			
			//FinalReport/Genes/
			Node genes = XMLParser.getNode("Genes", frNodes);
			List<FmReportGeneTb> geneList = new ArrayList<FmReportGeneTb>();
			if (genes != null) {
				List<Node> geneNodes = XMLParser.getNodes("Gene", genes.getChildNodes());
				if (geneNodes != null) {
					geneList = FmParseUtils.parseGenes(geneNodes, date, etlProcId, report);
				}
			}
			report.setFmReportGeneTbs(geneList);
			
			//FinalReport/References/
			Node refs = XMLParser.getNode("References", frNodes);
			List<FmReportReferenceTb> refList = new ArrayList<FmReportReferenceTb>();
			if (refs != null) {
				refList = FmParseUtils.parseReference(refs, date, etlProcId, report);
			}
			report.setFmReportReferenceTbs(refList);
			
			//FinalReport/Signatures/
			Node sigs = XMLParser.getNode("Signatures", frNodes);
			List<FmReportSignatureTb> sigList = new ArrayList<FmReportSignatureTb>();
			if (sigs != null) {
				sigList = FmParseUtils.parseSignature(sigs, date, etlProcId, report);
			}
			report.setFmReportSignatureTbs(sigList);
			
			//FinalReport/AAC/Amendmends/
			Node aac = XMLParser.getNode("AAC", frNodes);
			List<FmReportAmendmendTb> amendList = new ArrayList<FmReportAmendmendTb>();
			if (aac != null) {
				Node amend = XMLParser.getNode("Amendmends", aac.getChildNodes());
				if (amend != null) {
					amendList = FmParseUtils.parseAmendmend(amend, date, etlProcId, report);
				}
			}
			report.setFmReportAmendmendTbs(amendList);
//////////////////////////////////////////////////////////
			List<FmReportSampleTb> sampList = new ArrayList<FmReportSampleTb>();
			List<FmReportVarTb> varList = new ArrayList<FmReportVarTb>();
			if (variantReport != null) {
				NodeList vrNodes = variantReport.getChildNodes();
			
				//variant-report/samples/
				Node samples = XMLParser.getNode("samples", vrNodes);
				sampList = FmParseUtils.parseSample(samples, date, etlProcId, report);
	
				//variant-report/[short-variant|copy-number-alterations|rearrangements|non-human-content]				
				varList = FmParseUtils.parseVar(vrNodes, date, etlProcId, report);
			}
			report.setFmReportSampleTbs(sampList);
			report.setFmReportVarTbs(varList);
			
			
			// persist begins
			report.setSpecimenTb(specimenTb);
			List<FmReportTb> reports = new ArrayList<FmReportTb>();
			reports.add(report);
			specimenTb.setFmReportTbs(reports);
			
			SpecimenDao specimenDao = new SpecimenDao(em);
			specimenDao.persistSpecimen(specimenTb);
			em.close();
			emFactory.close();
	
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
}
