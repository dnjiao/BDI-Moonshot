package org.mdacc.rists.bdi.fm.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mdacc.rists.bdi.fm.models.FmReportAltPropertyTb;
import org.mdacc.rists.bdi.fm.models.FmReportAltTb;
import org.mdacc.rists.bdi.fm.models.FmReportAltTherapyTb;
import org.mdacc.rists.bdi.fm.models.FmReportAltTrialLkTb;
import org.mdacc.rists.bdi.fm.models.FmReportAmendmendTb;
import org.mdacc.rists.bdi.fm.models.FmReportAppTb;
import org.mdacc.rists.bdi.fm.models.FmReportGeneTb;
import org.mdacc.rists.bdi.fm.models.FmReportPertNegTb;
import org.mdacc.rists.bdi.fm.models.FmReportRefLkTb;
import org.mdacc.rists.bdi.fm.models.FmReportReferenceTb;
import org.mdacc.rists.bdi.fm.models.FmReportSampleTb;
import org.mdacc.rists.bdi.fm.models.FmReportSignatureTb;
import org.mdacc.rists.bdi.fm.models.FmReportTb;
import org.mdacc.rists.bdi.fm.models.FmReportTrialTb;
import org.mdacc.rists.bdi.fm.models.FmReportVarPropetyTb;
import org.mdacc.rists.bdi.fm.models.FmReportVarSampleTb;
import org.mdacc.rists.bdi.fm.models.FmReportVarTb;
import org.mdacc.rists.bdi.utils.XMLParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FmParseUtils {
	
	final static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	public static void main(String[] args) {
		
	}
	
	// XML Path: /FinalReport/Application/ApplicationSettings/ApplicationSetting
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
	
	// XML Path: /FinalReport/PertinentNegatives/PertinentNegative
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
	
	// XML Path: /FinalReport/VariantProperties/VariantProperty
	public static List<FmReportVarPropetyTb> parseVarProperty(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportVarPropetyTb> varPropList = new ArrayList<FmReportVarPropetyTb>();
		NodeList nodes = node.getChildNodes();
		List<Node> varProps = XMLParser.getNodes("VariantProperty", nodes);
		for (Node varProp : varProps) {
			FmReportVarPropetyTb vpTb = new FmReportVarPropetyTb();
			NodeList vpNodes = varProp.getChildNodes();
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
	
	// XML Path: /FinalReport/Genes/Gene/
	public static List<FmReportGeneTb> parseGenes(List<Node> genes, Date date, BigDecimal etl, FmReportTb report) {
			List<FmReportGeneTb> geneList = new ArrayList<FmReportGeneTb>();
			for (Node gene : genes) {
				FmReportGeneTb geneTb = new FmReportGeneTb();
				NodeList geneNodes = gene.getChildNodes();
				geneTb.setInsertTs(date);
				geneTb.setUpdateTs(date);
				geneTb.setEtlProcId(etl);
				geneTb.setFmReportTb(report);
				geneTb.setName(XMLParser.getNodeValue("Name", geneNodes));
				geneTb.setInclude(boolToChar(XMLParser.getNodeValue("Include", geneNodes)));
				// ReferenceLinks section
				Node rlNode = XMLParser.getNode("ReferenceLinks", geneNodes);
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
							rfList.add(refLk);
						}
					}
				}
				if (rfList != null) {
					geneTb.setFmReportRefLkTbs(rfList);
				}
				geneList.add(geneTb);
			}
			return geneList;
		}
		
	// XML Path: /FinalReport/Genes/Gene/Alterations/Alteration
	public static List<FmReportAltTb> parseAlt(List<Node> genes, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportAltTb> altList = new ArrayList<FmReportAltTb>();
		for (Node gene : genes) {
			NodeList geneNodes = gene.getChildNodes();
			Node alterations = XMLParser.getNode("Alterations", geneNodes);
			NodeList altNodes = alterations.getChildNodes();
			List<Node> alts = XMLParser.getNodes("Alteration", altNodes);
			for (Node alt : alts) {
				NodeList nl = alt.getChildNodes();
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
				// ReferenceLinks section
				Node rlNode = XMLParser.getNode("ReferenceLinks", geneNodes);
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
							refLk.setFmReportAltTb(altTb);
							rfList.add(refLk);
						}
					}
				}
				if (rfList != null) {
					altTb.setFmReportRefLkTbs(rfList);
				}
				altList.add(altTb);
			}
		}
		return altList;
	}
	
	// XML Path: /FinalReport/Genes/Gene/Alterations/Alteration/AlterationProperties/AlterationProperty 
	public static List<FmReportAltPropertyTb> parseAltProperty(List<Node> genes, Date date, BigDecimal etl, FmReportTb report, FmReportAltTb reportAlt) {

		List<FmReportAltPropertyTb> altPropertyList = new ArrayList<FmReportAltPropertyTb>();
		for (Node gene : genes) {
			NodeList geneNodes = gene.getChildNodes();
			Node alterations = XMLParser.getNode("Alterations", geneNodes);
			NodeList altNodes = alterations.getChildNodes();
			List<Node> alts = XMLParser.getNodes("Alteration", altNodes);
			for (Node alt : alts) {
				Node properties = XMLParser.getNode("AlterrationProperties", alt.getChildNodes());
				List<Node> altProps = XMLParser.getNodes("AlterationProperty", properties.getChildNodes());
				for (Node altProp : altProps) {
					FmReportAltPropertyTb altProperty = new FmReportAltPropertyTb();
					altProperty.setInsertTs(date);
					altProperty.setUpdateTs(date);
					altProperty.setEtlProcId(etl);
					altProperty.setFmReportTb(report);
					altProperty.setFmReportAltTb(reportAlt);
					altProperty.setIsEquivocal(boolToChar(XMLParser.getNodeAttr("isEquivocal", altProp)));
					altProperty.setIsSubclonal(XMLParser.getNodeAttr("isSubclonal", altProp));
					altProperty.setName(XMLParser.getNodeAttr("name", altProp));
					altPropertyList.add(altProperty);
				}
			}
		}
		return altPropertyList;
	}
	
	// XML Path: /FinalReport/Genes/Gene/Alterations/Alteration/Therapies/Therapy
	public static List<FmReportAltTherapyTb> parseAltTherapy(List<Node> genes, Date date, BigDecimal etl, FmReportTb report, FmReportAltTb reportAlt) {

		List<FmReportAltTherapyTb> altTherapyList = new ArrayList<FmReportAltTherapyTb>();
		for (Node gene : genes) {
			NodeList geneNodes = gene.getChildNodes();
			Node alterations = XMLParser.getNode("Alterations", geneNodes);
			NodeList altNodes = alterations.getChildNodes();
			List<Node> alts = XMLParser.getNodes("Alteration", altNodes);
			for (Node alt : alts) {
				Node therapiesNode = XMLParser.getNode("Therapies", alt.getChildNodes());
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
					Node rlNode = XMLParser.getNode("ReferenceLinks", geneNodes);
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
								refLk.setFmReportAltTherapyTb(altTherapy);
								rfList.add(refLk);
							}
						}
					}
					if (rfList != null) {
						altTherapy.setFmReportRefLkTbs(rfList);
					}
					altTherapyList.add(altTherapy);
				}
			}
		}
		return altTherapyList;
	}
	
	// XML Path: /FinalReport/Genes/Gene/; /FinalReport/Genes/Gene/Alterations/Alteration; /FinalReport/Genes/Gene/Alterations/Alteration/Therapies/Therapy
	
	// XML Path: /FinalReport/Genes/Gene/Alterations/Alteration/ClinicalTrialLinks/ClinicalTrialLink
	public static List<FmReportAltTrialLkTb> parseAltTrialLk(List<Node> genes, Date date, BigDecimal etl, FmReportTb report, FmReportAltTb reportAlt) {

		List<FmReportAltTrialLkTb> altTrialLkList = new ArrayList<FmReportAltTrialLkTb>();
		for (Node gene : genes) {
			NodeList geneNodes = gene.getChildNodes();
			Node alterations = XMLParser.getNode("Alterations", geneNodes);
			NodeList altNodes = alterations.getChildNodes();
			List<Node> alts = XMLParser.getNodes("Alteration", altNodes);
			for (Node alt : alts) {
				Node trialLinks = XMLParser.getNode("ClinicalTrialLinks", alt.getChildNodes());
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
		}
		return altTrialLkList;
	}
	
	// XML Path: /FinalReport/Trials
	public static List<FmReportTrialTb> parseTrials(Node node, Date date, BigDecimal etl, FmReportTb report) {
		NodeList nodes = node.getChildNodes();
		List<Node> trials = XMLParser.getNodes("Trial", nodes);
		List<FmReportTrialTb> reportTrialList = new ArrayList<FmReportTrialTb>();
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
			String refNum = XMLParser.getNodeAttr("number", ref);
			if (refNum != "") {
				refTb.setReferenceNumber(new BigDecimal(refNum));
			}
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
	
	// XML Path: /FinalReport/AAC/Amendmends/Amendmend
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
			NodeList sampleNodes = sample.getChildNodes();
			samp.setInsertTs(date);
			samp.setUpdateTs(date);
			samp.setEtlProcId(etl);
			samp.setFmReportTb(report);
			samp.setName(XMLParser.getNodeAttr("name", sample));
			samp.setTissue(XMLParser.getNodeAttr("tissue", sample));
			samp.setType(XMLParser.getNodeAttr("type", sample));
			String depth = XMLParser.getNodeAttr("mean-exon-depth", sample);
			if (depth != "") {
				samp.setMeanExonDepth(new BigDecimal(depth));
			}
			samp.setBaitSet(XMLParser.getNodeAttr("bait-set", sample));
			samp.setNucleicAcidType(XMLParser.getNodeAttr("nucleic-acid-type", sample));
			sampleList.add(samp);
		}
		return sampleList;
	}
	
	// XML Path: /variant-report/[short-variant|copy-number-alterations|rearrangements|non-human-content]
	public static List<FmReportVarTb> parseVar(Node node, Date date, BigDecimal etl, FmReportTb report) {
		List<FmReportVarTb> varList = new ArrayList<FmReportVarTb>();
		NodeList nodes = node.getChildNodes();
		//short-variant section
		Node shortVariants = XMLParser.getNode("short-variants", nodes);
		List<Node> svNodes = XMLParser.getNodes("short-variant", shortVariants.getChildNodes());
		for (Node sv : svNodes) {
			FmReportVarTb varTb = new FmReportVarTb();
			FmReportVarSampleTb varSample = new FmReportVarSampleTb();
			varTb.setInsertTs(date);
			varTb.setUpdateTs(date);
			varTb.setEtlProcId(etl);
			varTb.setFmReportTb(report);
			
			varTb.setType("short-variant");
			varTb.setVariantCdsEffect(XMLParser.getNodeAttr("cds-effect", sv));
			String depth = XMLParser.getNodeAttr("depth", sv);
			if (depth != "") {
				varTb.setVariantDepth(new BigDecimal(depth));
			}
			varTb.setVariantGeneName(XMLParser.getNodeAttr("gene", sv));
			String percent = XMLParser.getNodeAttr("percent-reads", sv);
			if (percent != "") {
				varTb.setVariantPercentReads(new BigDecimal(percent));
			}
			varTb.setVariantPosition(XMLParser.getNodeAttr("position", sv));
			varTb.setVariantProteinEffect(XMLParser.getNodeAttr("protein-effect", sv));
			varTb.setVariantStatus(XMLParser.getNodeAttr("status", sv));
			varTb.setVariantTranscript(XMLParser.getNodeAttr("transcript", sv));
			varTb.setVariantStrand(XMLParser.getNodeAttr("strand", sv));
			varTb.setVariantFunctionalEffect(XMLParser.getNodeAttr("functional-effect", sv));
			varTb.setVariantIsSubclonal(boolToChar(XMLParser.getNodeAttr("subclonal", sv)));
			Node varSampleNode = XMLParser.getNode("dna-evidence", sv.getChildNodes());
			varSample.setInsertTs(date);
			varSample.setUpdateTs(date);
			varSample.setEtlProcId(etl);
			varSample.setFmReportTb(report);
			varSample.setSampleName(XMLParser.getNodeAttr("sample", varSampleNode));
			varSample.setFmReportTb(report);
			varSample.setFmReportVarTb(varTb);
			varList.add(varTb);
		}
		
	}
	private static String boolToChar(String str) {
		if (str.equalsIgnoreCase("true")) {
			return "Y";
		} else if (str.equalsIgnoreCase("false")) {
			return "N";
		} else {
			return "";
		}
		
	}

}
