package org.mdacc.rists.bdi.fm.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mdacc.rists.bdi.models.FmReportAltPropertyTb;
import org.mdacc.rists.bdi.models.FmReportAltTb;
import org.mdacc.rists.bdi.models.FmReportAltTherapyTb;
import org.mdacc.rists.bdi.models.FmReportAltTrialLkTb;
import org.mdacc.rists.bdi.models.FmReportAmendmendTb;
import org.mdacc.rists.bdi.models.FmReportAppTb;
import org.mdacc.rists.bdi.models.FmReportGeneTb;
import org.mdacc.rists.bdi.models.FmReportPertNegTb;
import org.mdacc.rists.bdi.models.FmReportRefLkTb;
import org.mdacc.rists.bdi.models.FmReportReferenceTb;
import org.mdacc.rists.bdi.models.FmReportSampleTb;
import org.mdacc.rists.bdi.models.FmReportSignatureTb;
import org.mdacc.rists.bdi.models.FmReportTb;
import org.mdacc.rists.bdi.models.FmReportTrialTb;
import org.mdacc.rists.bdi.models.FmReportVarPropertyTb;
import org.mdacc.rists.bdi.models.FmReportVarSampleTb;
import org.mdacc.rists.bdi.models.FmReportVarTb;
import org.mdacc.rists.bdi.utils.XMLParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FmParseUtils {
	
	final static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	public static void main(String[] args) {
		String str = "CDK2A/B";
		System.out.println(parseGeneName(str));
	}
	
	
	/**
	 * Parse "Application" section into fm_report_app_tb
	 * @param node -- /FinalReport/Application/ApplicationSettings
	 */
	public static List<FmReportAppTb> parseApplication(Node node, Date date, BigDecimal etl, FmReportTb report) {
		
		List<FmReportAppTb> reportAppList = new ArrayList<FmReportAppTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> settings = XMLParser.getNodes("ApplicationSetting", nodes);
		for (Node setting : settings) {
			FmReportAppTb appSetting = new FmReportAppTb();
			NodeList setNodes = setting.getChildNodes();
			appSetting.setInsertTs(date);
			appSetting.setUpdateTs(date);
			appSetting.setEtlProcId(etl);
			appSetting.setFmReportTb(report);
			appSetting.setName(XMLParser.getNodeValue("Name", setNodes));
			appSetting.setValue(XMLParser.getNodeValue("Value", setNodes));
			reportAppList.add(appSetting);
		}
		return reportAppList;
	}
	
	/**
	 * Parse "PertinentNegatives" node to fm_report_pert_neg_tb
	 * @param node -- /FinalReport/PertinentNegatives
	 */
	public static List<FmReportPertNegTb> parsePert(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportPertNegTb> pertNegList = new ArrayList<FmReportPertNegTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> pertNegs = XMLParser.getNodes("PertinentNegative", nodes);
		for (Node pertNeg : pertNegs) {
			FmReportPertNegTb pert = new FmReportPertNegTb();
			NodeList pertNodes = pertNeg.getChildNodes();
			pert.setInsertTs(date);
			pert.setUpdateTs(date);
			pert.setEtlProcId(etl);
			pert.setFmReportTb(report);
			pert.setGeneName(XMLParser.getNodeValue("Gene", pertNodes));
			pertNegList.add(pert);
		}
		return pertNegList;
	}
	
	/**
	 * Parse "VariantProperties" into fm_report_var_property_tb
	 * @param node -- /FinalReport/VariantProperties
	 */
	public static List<FmReportVarPropertyTb> parseVarProperty(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportVarPropertyTb> varPropList = new ArrayList<FmReportVarPropertyTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> varProps = XMLParser.getNodes("VariantProperty", nodes);
		for (Node varProp : varProps) {
			FmReportVarPropertyTb vpTb = new FmReportVarPropertyTb();
			vpTb.setInsertTs(date);
			vpTb.setUpdateTs(date);
			vpTb.setEtlProcId(etl);
			vpTb.setFmReportTb(report);
			vpTb.setGeneName(XMLParser.getNodeAttr("geneName", varProp));
			vpTb.setIsVus(boolToChar(XMLParser.getNodeAttr("isVUS", varProp)));
			vpTb.setVariantName(XMLParser.getNodeAttr("variantName", varProp));
			varPropList.add(vpTb);
		}
		return varPropList;
	}
	
	/**
	 * Parse "Genes" node into fm_report_gene_tb, all alt tables
	 * @param genes -- List of "Gene" nodes in /FinalReport/Genes/Gene/
	 */
	public static List<FmReportGeneTb> parseGenes(List<Node> genes, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportGeneTb> geneList = new ArrayList<FmReportGeneTb>();
		for (Node gene : genes) {
			NodeList geneNodes = gene.getChildNodes();
			Node altNode = XMLParser.getNode("Alterations", geneNodes);
			Node rlNode = XMLParser.getNode("ReferenceLinks", geneNodes);
			String name = XMLParser.getNodeValue("Name", geneNodes);
			List<String> geneNames = parseGeneName(name);
			for (String geneName : geneNames) {
				FmReportGeneTb geneTb = new FmReportGeneTb();
				geneTb.setInsertTs(date);
				geneTb.setUpdateTs(date);
				geneTb.setEtlProcId(etl);
				geneTb.setFmReportTb(report);
				geneTb.setName(geneName);
				geneTb.setInclude(boolToChar(XMLParser.getNodeValue("Include", geneNodes)));
				
				// Alterations section
				List<FmReportAltTb> altList = new ArrayList<FmReportAltTb>();
				if (altNode != null) {
					List<Node> alterations = XMLParser.getNodes("Alteration", altNode.getChildNodes());
					for (Node alteration : alterations) {
						NodeList nl = alteration.getChildNodes();
						FmReportAltTb altTb = new FmReportAltTb();
						altTb.setInsertTs(date);
						altTb.setUpdateTs(date);
						altTb.setEtlProcId(etl);
						altTb.setFmReportTb(report);
						altTb.setName(XMLParser.getNodeValue("Name", nl));
						altTb.setRelavance(XMLParser.getNodeValue("Relavance", nl));
						altTb.setInterpretation(XMLParser.getNodeValue("Interpretation", nl));
						altTb.setIndication(XMLParser.getNodeValue("Indication", nl));
						altTb.setTrialNote(XMLParser.getNodeValue("ClinicalTrialNote", nl));
						altTb.setInclude(boolToChar(XMLParser.getNodeValue("Include", nl)));
						List<FmReportAltPropertyTb> apList = parseAltProperty(alteration, date, etl, report, altTb);
						altTb.setFmReportAltPropertyTbs(apList);
						List<FmReportRefLkTb> rfList = parseRefLkFromAlteration(nl, date, etl, report, altTb);
						altTb.setFmReportRefLkTbs(rfList);
						List<FmReportAltTherapyTb> atList = parseAltTherapy(alteration, date, etl, report, altTb);
						altTb.setFmReportAltTherapyTbs(atList);
						List<FmReportAltTrialLkTb> atlList = parseAltTrialLk(alteration, date, etl, report, altTb);
						altTb.setFmReportAltTrialLkTbs(atlList);
						altTb.setFmReportGeneTb(geneTb);
						altList.add(altTb);
					}
				}
				geneTb.setFmReportAltTbs(altList);
				// ReferenceLinks section
				List<FmReportRefLkTb> rfList = new ArrayList<FmReportRefLkTb>();
				if (rlNode != null) {
					List<Node> reflinks = XMLParser.getNodes("ReferenceLink", rlNode.getChildNodes());
					if (reflinks != null) {
						for (Node reflink : reflinks) {
							FmReportRefLkTb refLk = new FmReportRefLkTb();
							refLk.setInsertTs(date);
							refLk.setUpdateTs(date);
							refLk.setEtlProcId(etl);
							refLk.setFmReportTb(report);
							refLk.setReferenceId(XMLParser.getNodeAttr("referenceId", reflink));
							refLk.setInclude(boolToChar(XMLParser.getNodeValue("Include", reflink.getChildNodes())));
							refLk.setFmReportGeneTb(geneTb);
							refLk.setFmReportTb(report);
							rfList.add(refLk);
						}
					}
				}
				if (rfList != null) {
					geneTb.setFmReportRefLkTbs(rfList);
				}
				
				geneList.add(geneTb);
			}
		}
		return geneList;
	}
		
	private static List<String> parseGeneName(String name) {
		List<String> gNames = new ArrayList<String>();
		if (name.indexOf('/') == name.length() - 2) {
			String[] items = name.split("/");
			gNames.add(items[0]);
			gNames.add(items[0].substring(0, items[0].length() - 1) + items[1]);
		}
		else {
			gNames.add(name);
		}
		return gNames;
	}


	/**
	 * parse reference links from /FinalReport/Genes/Gene/Alterations/Alteration section
	 * @param nl - NodeList of ReferenceLinks
	 */
	private static List<FmReportRefLkTb> parseRefLkFromAlteration(NodeList nl, Date date, BigDecimal etl, FmReportTb report,
			FmReportAltTb altTb) {
		List<FmReportRefLkTb> rfList = new ArrayList<FmReportRefLkTb>();
		Node rlNode = XMLParser.getNode("ReferenceLinks", nl);
		if (rlNode != null) {
			List<Node> reflinks = XMLParser.getNodes("ReferenceLink", rlNode.getChildNodes());
			if (reflinks != null) {				
				for (Node reflink : reflinks) {
					FmReportRefLkTb refLk = new FmReportRefLkTb();
					refLk.setInsertTs(date);
					refLk.setUpdateTs(date);
					refLk.setEtlProcId(etl);
					refLk.setFmReportTb(report);
					refLk.setReferenceId(XMLParser.getNodeAttr("referenceId", reflink));
					refLk.setInclude(boolToChar(XMLParser.getNodeValue("Include", reflink.getChildNodes())));
					refLk.setFmReportAltTb(altTb);
					refLk.setFmReportTb(report);
					rfList.add(refLk);
				}
			}
		}
		return rfList;
	}
	
	/**
	 * Parse "AlterationProperties" into fm_report_alt_property_tb
	 * @param node /FinalReport/Genes/Gene/Alterations/Alteration
	 */
	public static List<FmReportAltPropertyTb> parseAltProperty(Node node, Date date, BigDecimal etl, FmReportTb report, FmReportAltTb reportAlt) {

		List<FmReportAltPropertyTb> altPropertyList = new ArrayList<FmReportAltPropertyTb>();
		Node properties = XMLParser.getNode("AlterationProperties", node.getChildNodes());
		if (properties != null) {
			List<Node> altProps = XMLParser.getNodes("AlterationProperty", properties.getChildNodes());
			for (Node altProp : altProps) {
				FmReportAltPropertyTb altProperty = new FmReportAltPropertyTb();
				altProperty.setInsertTs(date);
				altProperty.setUpdateTs(date);
				altProperty.setEtlProcId(etl);
				altProperty.setFmReportTb(report);
				altProperty.setFmReportAltTb(reportAlt);
				altProperty.setIsEquivocal(boolToChar(XMLParser.getNodeAttr("isEquivocal", altProp)));
				altProperty.setIsSubclonal(boolToChar(XMLParser.getNodeAttr("isSubclonal", altProp)));
				altProperty.setName(XMLParser.getNodeAttr("name", altProp));
				altPropertyList.add(altProperty);
			}
		}
		return altPropertyList;
	}
	
	/**
	 * parse "Theparies" into fm_report_alt_therapy_tb
	 * @param node - /FinalReport/Genes/Gene/Alterations/Alteration
	 */
	public static List<FmReportAltTherapyTb> parseAltTherapy(Node node, Date date, BigDecimal etl, FmReportTb report, FmReportAltTb reportAlt) {
		List<FmReportAltTherapyTb> altTherapyList = new ArrayList<FmReportAltTherapyTb>();
		Node therapiesNode = XMLParser.getNode("Therapies", node.getChildNodes());
		List<Node> therapies = XMLParser.getNodes("Therapy", therapiesNode.getChildNodes());
		for (Node therapy : therapies) {
			NodeList nl = therapy.getChildNodes();
			FmReportAltTherapyTb altTherapy = new FmReportAltTherapyTb();
			altTherapy.setInsertTs(date);
			altTherapy.setUpdateTs(date);
			altTherapy.setEtlProcId(etl);
			altTherapy.setFmReportTb(report);
			altTherapy.setFmReportAltTb(reportAlt);
			altTherapy.setName(XMLParser.getNodeValue("Name", nl));
			altTherapy.setGenericName(XMLParser.getNodeValue("GenericName", nl));
			altTherapy.setFdaApproved(boolToChar(XMLParser.getNodeValue("FDAApproved", nl)));
			altTherapy.setRationale(XMLParser.getNodeValue("Rationale", nl));
			altTherapy.setApprovedUses(XMLParser.getNodeValue("ApprovedUses", nl));
			altTherapy.setEffect(XMLParser.getNodeValue("Effect", nl));
			altTherapy.setInclude(boolToChar(XMLParser.getNodeValue("Include", nl)));
			altTherapy.setIncludeInSummary(boolToChar(XMLParser.getNodeValue("IncludeInSummary", nl)));
			// ReferenceLinks section
			List<FmReportRefLkTb> rfList = parseRefLkFromTherapy(therapy.getChildNodes(), date, etl, report, altTherapy);
			if (rfList != null) {
				altTherapy.setFmReportRefLkTbs(rfList);
			}
			altTherapyList.add(altTherapy);
		}
		return altTherapyList;
	}
	
	/**
	 * parse reference links from /FinalReport/Genes/Gene/Alterations/Alteration/Therapies/Therapy section
	 * @param nl - NodeList of ReferenceLinks
	 */
	private static List<FmReportRefLkTb> parseRefLkFromTherapy(NodeList nl, Date date, BigDecimal etl, FmReportTb report,
			FmReportAltTherapyTb atTb) {
		List<FmReportRefLkTb> rfList = new ArrayList<FmReportRefLkTb>();
		Node rlNode = XMLParser.getNode("ReferenceLinks", nl);
		if (rlNode != null) {
			List<Node> reflinks = XMLParser.getNodes("ReferenceLink", rlNode.getChildNodes());
			if (reflinks != null) {
				
				for (Node reflink : reflinks) {
					FmReportRefLkTb refLk = new FmReportRefLkTb();
					refLk.setInsertTs(date);
					refLk.setUpdateTs(date);
					refLk.setEtlProcId(etl);
					refLk.setFmReportTb(report);
					refLk.setReferenceId(XMLParser.getNodeAttr("referenceId", reflink));
					refLk.setInclude(boolToChar(XMLParser.getNodeValue("Include", reflink.getChildNodes())));
					refLk.setFmReportAltTherapyTb(atTb);
					refLk.setFmReportTb(report);
					rfList.add(refLk);
				}
			}
		}
		return rfList;
	}	
	
	/**
	 * parse "ClinicalTrialLinks" into fm_report_alt_trial_lk_tb
	 * @param node - /FinalReport/Genes/Gene/Alterations/Alteration
	 */
	public static List<FmReportAltTrialLkTb> parseAltTrialLk(Node node, Date date, BigDecimal etl, FmReportTb report, FmReportAltTb reportAlt) {

		List<FmReportAltTrialLkTb> altTrialLkList = new ArrayList<FmReportAltTrialLkTb>();
		Node trialLinks = XMLParser.getNode("ClinicalTrialLinks", node.getChildNodes());
		if (trialLinks != null) {
			List<Node> links = XMLParser.getNodes("ClinicalTrialLink", trialLinks.getChildNodes());
			for (Node link : links) {
				FmReportAltTrialLkTb altTrialLk = new FmReportAltTrialLkTb();
				altTrialLk.setInsertTs(date);
				altTrialLk.setUpdateTs(date);
				altTrialLk.setEtlProcId(etl);
				altTrialLk.setFmReportTb(report);
				altTrialLk.setFmReportAltTb(reportAlt);
				altTrialLk.setNctId(XMLParser.getNodeAttr("nctId", link));
				altTrialLkList.add(altTrialLk);
			}
		}		
		return altTrialLkList;
	}
	
	// XML Path: /FinalReport/Trials
	public static List<FmReportTrialTb> parseTrials(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportTrialTb> reportTrialList = new ArrayList<FmReportTrialTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> trials = XMLParser.getNodes("Trial", nodes);
		for (Node trial : trials) {
			NodeList trialNodes = trial.getChildNodes();
			FmReportTrialTb reportTrial = new FmReportTrialTb();
			reportTrial.setGeneName(XMLParser.getNodeValue("Gene", trialNodes));
			reportTrial.setAlterationName(XMLParser.getNodeValue("Alteration", trialNodes));
			reportTrial.setTitle(XMLParser.getNodeValue("Title", trialNodes));
			reportTrial.setSummary(XMLParser.getNodeValue("Summary", trialNodes));
			reportTrial.setStudyPhase(XMLParser.getNodeValue("StudyPhase", trialNodes));
			reportTrial.setCondition(XMLParser.getNodeValue("Condition", trialNodes));
			reportTrial.setTarget(XMLParser.getNodeValue("Target", trialNodes));
			reportTrial.setEligibility(XMLParser.getNodeValue("Eligibility", trialNodes));
			reportTrial.setLocations(XMLParser.getNodeValue("Locations", trialNodes));
			reportTrial.setNctId(XMLParser.getNodeValue("NCTID", trialNodes));
			reportTrial.setNote(XMLParser.getNodeValue("Note", trialNodes));
			reportTrial.setInclude(boolToChar(XMLParser.getNodeValue("Include", trialNodes)));
			reportTrial.setSummary(XMLParser.getNodeValue("Summary", trialNodes));
			reportTrial.setInsertTs(date);
			reportTrial.setUpdateTs(date);
			reportTrial.setEtlProcId(etl);
			reportTrial.setFmReportTb(report);
			reportTrialList.add(reportTrial);
		}
		return reportTrialList;	
	}
		
	// XML Path: /FinalReport/References
	public static List<FmReportReferenceTb> parseReference(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportReferenceTb> reportReferenceList = new ArrayList<FmReportReferenceTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> references = XMLParser.getNodes("Reference", nodes);
		for (Node ref : references) {
			FmReportReferenceTb refTb = new FmReportReferenceTb();
			NodeList refNodes = ref.getChildNodes();
			refTb.setInsertTs(date);
			refTb.setUpdateTs(date);
			refTb.setEtlProcId(etl);
			refTb.setFmReportTb(report);
			refTb.setReferenceNumber(strToNum(XMLParser.getNodeAttr("number", ref)));
			refTb.setReferenceId(XMLParser.getNodeValue("ReferenceId", refNodes));
			refTb.setCitation(XMLParser.getNodeValue("FullCitation", refNodes));
			refTb.setInclude(boolToChar(XMLParser.getNodeValue("Include", refNodes)));
			reportReferenceList.add(refTb);
		}
		return reportReferenceList;
	}
	
	// XML Path: /FinalReport/Signatures/Signature
	public static List<FmReportSignatureTb> parseSignature(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportSignatureTb> reportSignatureList = new ArrayList<FmReportSignatureTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> signatures = XMLParser.getNodes("Signature", nodes);
		for (Node signature : signatures) {
			FmReportSignatureTb sigTb = new FmReportSignatureTb();
			NodeList sigNodes = signature.getChildNodes();
			sigTb.setInsertTs(date);
			sigTb.setUpdateTs(date);
			sigTb.setEtlProcId(etl);
			sigTb.setFmReportTb(report);
			String serverTime = XMLParser.getNodeValue("ServerTime", sigNodes);
			try {
				if (serverTime != "") {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					sigTb.setServerTime(formatter.parse(serverTime));
				} 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			sigTb.setOpName(XMLParser.getNodeValue("OpName", sigNodes));
			sigTb.setText(XMLParser.getNodeValue("Text", sigNodes));
			reportSignatureList.add(sigTb);
		}
		return reportSignatureList;
	}
	
	/**
	 * parse "Amendmends" section into fm_report_amendmend_tb
	 * @param node - /FinalReport/AAC/Amendmends
	 */
	public static List<FmReportAmendmendTb> parseAmendmend(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportAmendmendTb> reportAmendList = new ArrayList<FmReportAmendmendTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> amendmends = XMLParser.getNodes("Amendmend", nodes);
		for (Node amendmend : amendmends) {
			FmReportAmendmendTb amend = new FmReportAmendmendTb();
			NodeList amendNodes = amendmend.getChildNodes();
			amend.setInsertTs(date);
			amend.setUpdateTs(date);
			amend.setEtlProcId(etl);
			amend.setFmReportTb(report);
			String modDate = XMLParser.getNodeValue("ModifiedDts", amendNodes);
			try {
				if (modDate != "") {
					amend.setModifyDate(FORMATTER.parse(modDate));
				} 
			} catch (ParseException e) {
				e.printStackTrace();
			}
			amend.setIsSigned(boolToChar(XMLParser.getNodeValue("IsSigned", amendNodes)));
			amend.setAmendmendComments(XMLParser.getNodeValue("Comment", amendNodes));
			amend.setText(XMLParser.getNodeValue("Text", amendNodes));
			reportAmendList.add(amend);
		}
		return reportAmendList;
	}

	// XML Path: /variant-report/samples/sample
	public static List<FmReportSampleTb> parseSample(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportSampleTb> sampleList = new ArrayList<FmReportSampleTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> samples = XMLParser.getNodes("sample", nodes);
		for (Node sample : samples) {
			FmReportSampleTb samp = new FmReportSampleTb();
			samp.setInsertTs(date);
			samp.setUpdateTs(date);
			samp.setEtlProcId(etl);
			samp.setFmReportTb(report);
			samp.setName(XMLParser.getNodeAttr("name", sample));
			samp.setTissue(XMLParser.getNodeAttr("tissue", sample));
			samp.setType(XMLParser.getNodeAttr("type", sample));
			samp.setMeanExonDepth(strToNum(XMLParser.getNodeAttr("mean-exon-depth", sample)));
			samp.setBaitSet(XMLParser.getNodeAttr("bait-set", sample));
			samp.setNucleicAcidType(XMLParser.getNodeAttr("nucleic-acid-type", sample));
			sampleList.add(samp);
		}
		return sampleList;
	}
	
	// XML Path: /variant-report/[short-variant|copy-number-alterations|rearrangements|non-human-content]
	public static List<FmReportVarTb> parseVar(NodeList nodes, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportVarTb> varList = new ArrayList<FmReportVarTb>();
		//short-variant section
		Node shortVariants = XMLParser.getNode("short-variants", nodes);
		List<Node> svNodes = XMLParser.getNodes("short-variant", shortVariants.getChildNodes());
		if (svNodes != null) {
			for (Node sv : svNodes) {
				FmReportVarTb varTb = new FmReportVarTb();
				
				List<FmReportVarSampleTb> varSampleList = new ArrayList<FmReportVarSampleTb>();
				varTb.setInsertTs(date);
				varTb.setUpdateTs(date);
				varTb.setEtlProcId(etl);
				varTb.setFmReportTb(report);
				
				varTb.setType("short-variant");
				varTb.setVariantCdsEffect(XMLParser.getNodeAttr("cds-effect", sv));
				varTb.setVariantDepth(strToNum(XMLParser.getNodeAttr("depth", sv)));
				varTb.setVariantGeneName(XMLParser.getNodeAttr("gene", sv));
				varTb.setVariantPercentReads(strToNum(XMLParser.getNodeAttr("percent-reads", sv)));
				varTb.setVariantPosition(XMLParser.getNodeAttr("position", sv));
				varTb.setVariantProteinEffect(XMLParser.getNodeAttr("protein-effect", sv));
				varTb.setVariantStatus(XMLParser.getNodeAttr("status", sv));
				varTb.setVariantTranscript(XMLParser.getNodeAttr("transcript", sv));
				varTb.setVariantStrand(XMLParser.getNodeAttr("strand", sv));
				varTb.setVariantFunctionalEffect(XMLParser.getNodeAttr("functional-effect", sv));
				varTb.setVariantIsSubclonal(boolToChar(XMLParser.getNodeAttr("subclonal", sv)));
				Node varSampleNode = XMLParser.getNode("dna-evidence", sv.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varSampleNode = XMLParser.getNode("rna-evidence", sv.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varTb.setFmReportVarSampleTbs(varSampleList);
				varList.add(varTb);
			}
		}
		
		// copy-number-alterations section
		Node copyNumAlt = XMLParser.getNode("copy-number-alterations", nodes);
		List<Node> cnNodes = XMLParser.getNodes("copy-number-alteration", copyNumAlt.getChildNodes());
		if (cnNodes != null) {
			for (Node cna : cnNodes) {
				FmReportVarTb varTb = new FmReportVarTb();
				List<FmReportVarSampleTb> varSampleList = new ArrayList<FmReportVarSampleTb>();
				varTb.setInsertTs(date);
				varTb.setUpdateTs(date);
				varTb.setEtlProcId(etl);
				varTb.setFmReportTb(report);
				
				varTb.setType("copy-number-alteration");
				varTb.setCopyNumberValue(strToNum(XMLParser.getNodeAttr("copy-number", cna)));
				varTb.setCopyNumberGeneName(XMLParser.getNodeAttr("gene", cna));
				varTb.setCopyNumberNumberOfExons(XMLParser.getNodeAttr("number-of-exons", cna));
				varTb.setCopyNumberPosition(XMLParser.getNodeAttr("position", cna));
				varTb.setCopyNumberType(XMLParser.getNodeAttr("type", cna));
				varTb.setCopyNumberRatio(strToNum(XMLParser.getNodeAttr("ratio", cna)));
				varTb.setCopyNumberStatus(XMLParser.getNodeAttr("status", cna));
				varTb.setCopyNumberSegmentLength(strToNum(XMLParser.getNodeAttr("segment-length", cna)));
				varTb.setCopyNumberIsEquivocal(boolToChar(XMLParser.getNodeAttr("equivocal", cna)));
		
				Node varSampleNode = XMLParser.getNode("dna-evidence", cna.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varSampleNode = XMLParser.getNode("rna-evidence", cna.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varTb.setFmReportVarSampleTbs(varSampleList);
				varList.add(varTb);
			}
		}
		
		// rearrangements section
		Node rearrangement = XMLParser.getNode("rearrangements", nodes);
		List<Node> raNodes = XMLParser.getNodes("rearrangement", rearrangement.getChildNodes());
		if (raNodes != null) {
			for (Node ra : raNodes) {
				FmReportVarTb varTb = new FmReportVarTb();
				List<FmReportVarSampleTb> varSampleList = new ArrayList<FmReportVarSampleTb>();
				varTb.setInsertTs(date);
				varTb.setUpdateTs(date);
				varTb.setEtlProcId(etl);
				varTb.setFmReportTb(report);
				
				varTb.setType("rearrangement");
				varTb.setRearrangementInFrame(XMLParser.getNodeAttr("in-frame", ra));
				varTb.setRearrangementTargetedGene(XMLParser.getNodeAttr("targeted-gene", ra));
				varTb.setRearrangementOtherGene(XMLParser.getNodeAttr("other-gene", ra));
				varTb.setRearrangementPosition1(XMLParser.getNodeAttr("pos1", ra));
				varTb.setRearrangementPosition2(XMLParser.getNodeAttr("pos2", ra));
				varTb.setRearrangementReadPairs(strToNum(XMLParser.getNodeAttr("supporting-read-pairs", ra)));
				varTb.setRearrangementDescription(XMLParser.getNodeAttr("description", ra));
				varTb.setRearrangementStatus(XMLParser.getNodeAttr("status", ra));
				
				Node varSampleNode = XMLParser.getNode("dna-evidence", ra.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varSampleNode = XMLParser.getNode("rna-evidence", ra.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varTb.setFmReportVarSampleTbs(varSampleList);
				varList.add(varTb);
			}
		}
				
		// non-human section
		Node nonhuman = XMLParser.getNode("non-human-content", nodes);
		List<Node> nhNodes = XMLParser.getNodes("non-human", nonhuman.getChildNodes());
		if (nhNodes != null) {
			for (Node nh : nhNodes) {
				FmReportVarTb varTb = new FmReportVarTb();
				List<FmReportVarSampleTb> varSampleList = new ArrayList<FmReportVarSampleTb>();
				varTb.setInsertTs(date);
				varTb.setUpdateTs(date);
				varTb.setEtlProcId(etl);
				varTb.setFmReportTb(report);
				
				varTb.setType("non-human");
				varTb.setNonHumanOrganism(XMLParser.getNodeAttr("organism", nh));
				varTb.setNonHumanReadsPerMillion(strToNum(XMLParser.getNodeAttr("reads-per-million", nh)));
				varTb.setNonHumanStatus(XMLParser.getNodeAttr("status", nh));
				
				Node varSampleNode = XMLParser.getNode("dna-evidence", nh.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varSampleNode = XMLParser.getNode("rna-evidence", nh.getChildNodes());
				if (varSampleNode != null) {
					FmReportVarSampleTb varSample = new FmReportVarSampleTb();
					varSample.setInsertTs(date);
					varSample.setUpdateTs(date);
					varSample.setEtlProcId(etl);
					varSample.setFmReportTb(report);
					varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
					varSample.setFmReportTb(report);
					varSample.setFmReportVarTb(varTb);
					varSampleList.add(varSample);
				}
				varTb.setFmReportVarSampleTbs(varSampleList);
				varList.add(varTb);
			}
		}
		return varList;
	}
	
	// boolean to Char
	private static String boolToChar(String str) {
		if (str.equalsIgnoreCase("true")) {
			return "Y";
		} else if (str.equalsIgnoreCase("false")) {
			return "N";
		} else {
			return "";
		}
	}
	
	//String to BigDecimal
	private static BigDecimal strToNum(String str) {
		try {
			return new BigDecimal(str);
		} catch (NumberFormatException ex) {
//			System.out.println("Cannot convert " + str + " to number.");
			return null;
		}
	}
}
