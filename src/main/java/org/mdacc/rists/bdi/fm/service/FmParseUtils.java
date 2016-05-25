package org.mdacc.rists.bdi.fm.service;

import java.util.ArrayList;
import java.util.List;

import org.mdacc.rists.bdi.fm.models.FmReportTrialTb;
import org.mdacc.rists.bdi.utils.XMLParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FmParseUtils {
	
	public static List<FmReportTrialTb> parseTrials(Node node) {
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
			reportTrial.setInclude(XMLParser.getNodeValue("Include", trialNodes));
			reportTrial.setSummary(XMLParser.getNodeValue("Summary", trialNodes));
			reportTrialList.add(reportTrial);
		}
		return reportTrialList;
		
	}

}
