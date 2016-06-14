package org.mdacc.rists.bdi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import org.joda.time.DateTime;
import org.mdacc.rists.bdi.db.models.FileQueueResult;
import org.mdacc.rists.bdi.db.utils.DBConnection;
import org.mdacc.rists.bdi.db.utils.FileQueueUtil;
import org.mdacc.rists.bdi.fm.dao.FileLoadDao;
import org.mdacc.rists.bdi.fm.dao.SpecimenDao;
import org.mdacc.rists.bdi.fm.models.FileLoadTb;
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
import org.mdacc.rists.bdi.fm.service.FmParseUtils;
import org.mdacc.rists.bdi.utils.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SaveFmReports {
	public static void main (String args[]) throws ParserConfigurationException, SAXException, IOException {
		
		Connection conn = DBConnection.getConnection();
        // call stored procedure to get unsent files by type
		
		List<FileQueueResult> fqList = FileQueueUtil.getUnloaded(conn, "fm-xml");
		if (fqList == null) {
			System.out.println("No foundation files to load.");
			return;
		}
		// loop thru results
		int counter = 0;
		for (FileQueueResult fq : fqList) {
			int fileQueueId = fq.getRowId();
			String filepath = fq.getFileUri();
			File file = new File(filepath);
			if (!file.exists()) {
				System.err.println(filepath + " does not exist.");
			}
			else {
				if (variantExists(file)) {
					BigDecimal etl = getNextValue("ETL_PROC_SEQ");
					BigDecimal fileLoadId;
					Long flId = insertFileLoadTb(fileQueueId, filepath, etl);
					if (flId > 0) {
						fileLoadId = new BigDecimal(flId);
						if (insertReportTb(file, etl, fileLoadId)) {
							Date now = new Date();
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							System.out.println(file.getName() + " loaded successful with fileLoadId " + Long.toString(flId) + " at " + df.format(now));
							counter ++;
							setLoadStatus(conn, "S", fileQueueId, fileLoadId);
						} else {
							setLoadStatus(conn, "E", fileQueueId, fileLoadId);
						}
					}
				}
				else {
					Date now = new Date();
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println("No variant-report found in " + file.getName() + " at " + df.format(now));
				}
			}
		}
		System.out.println("Total " + counter + " FM files loaded to database.");
	}

	/**
	 * Get next value in sequence
	 * @param seq - sequence name in db
	 * @return
	 */
	private static BigDecimal getNextValue(String seq) {
		EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("RIStore_Flow");
		EntityManager em = emFactory.createEntityManager();
		String query = "SELECT " + seq + ".nextval from DUAL";
		Query q = em.createNativeQuery(query);
		BigDecimal val = (BigDecimal)q.getSingleResult();
		em.close();
		emFactory.close();
		return val;
	}
	
	private static long insertFileLoadTb(int fileQueueId, String filepath, BigDecimal etl) {
		
		long rowId = getNextValue("FILE_LOAD_TB_SEQ").longValue();
		BigDecimal fqId = new BigDecimal(String.valueOf(fileQueueId));
		BigDecimal fileTypeId = new BigDecimal(String.valueOf(10));
		BigDecimal seqNum = new BigDecimal(String.valueOf(1));
		File file = new File(filepath);
		String fileName = file.getName();
		Date date = new Date();
		FileLoadTb fileLoad = new FileLoadTb(rowId, etl, fqId, fileTypeId, date, fileName, seqNum, date, filepath);
		EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("RIStore_Flow");
		EntityManager em = emFactory.createEntityManager();
		FileLoadDao flDao = new FileLoadDao(em);
		boolean success = flDao.persistFileLoad(fileLoad);
		em.close();
		emFactory.close();
		if (success == true) {
			return rowId;
		}
		return 0;
	}


	public static boolean insertReportTb (File file, BigDecimal etlProcId, BigDecimal flId) {
	
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			
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
			report.setFileLoadId(flId);
			List<FmReportTb> reports = new ArrayList<FmReportTb>();
			reports.add(report);
			specimenTb.setFmReportTbs(reports);
			specimenTb.setFileLoadId(flId);
			
			EntityManagerFactory emFactory = Persistence.createEntityManagerFactory("RIStore_Flow");
			EntityManager em = emFactory.createEntityManager();
			SpecimenDao specimenDao = new SpecimenDao(em);
			boolean success = specimenDao.persistSpecimen(specimenTb);
			em.close();
			emFactory.close();
			if (success == true) {
				return true;
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return false;
	}
	
	/**
	 * determine if variant-report section exists in report
	 * @param file 
	 * @return - true for exists, false for not
	 */
	private static boolean variantExists(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("variant-report")) {
					return true;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Update load_status in file_queue_id and file_load_id after loading fm report into tables
	 * @param conn - db connection
	 * @param status - 'S' for success, 'E' for error
	 * @param fileQueueId - row_id in file_queue_tb
	 * @param fileLoadId - row_id in file_load_tb
	 */
	private static void setLoadStatus(Connection conn, String status, int fileQueueId, BigDecimal fileLoadId) {
		Date date = new Date();
		try {
			//update file_queue_tb
			String sql = "Update FILE_QUEUE_TB set LOAD_STATUS = ?, UPDATE_TS = ? where ROW_ID = ? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, status);
			ps.setDate(2, new java.sql.Date(date.getTime()));
			ps.setInt(3, fileQueueId);
			ps.executeUpdate();
			System.out.println("Update Load_Status in FILE_QUEUE_ID table: " + status);
			
			//update file_load_tb
			sql = "Update FILE_LOAD_TB set LOAD_STATUS = ?, UPDATE_TS = ? where ROW_ID = ? ";
			ps = conn.prepareStatement(sql);
			ps.setString(1, status);
			ps.setDate(2, new java.sql.Date(date.getTime()));
			ps.setInt(3, Integer.valueOf(fileLoadId.intValue()));
			ps.executeUpdate();
			System.out.println("Update Load_Status in FILE_LOAD_ID table: " + status);
			ps.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
	}
}
