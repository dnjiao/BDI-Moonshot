package org.mdacc.rists.bdi.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mdacc.rists.bdi.models.FoundationXML;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParseXML {
	public static void main(String[] args) throws Exception{
//		File file = new File(args[0]);
//		FmXMLParser(file);
		readSourceXML(args[0], args[1]);
	}
	
	public static List<String> readSourceXML(String path, String type) throws Exception{
		File file = new File(path);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		List<Node> flows = getNodes("Flow", doc.getDocumentElement().getChildNodes());
		List<String> sources = new ArrayList<String>();
		for (Node flow : flows) {
			NodeList flowNodes = flow.getChildNodes();
			String datatype = getNodeValues("datatype", flowNodes).get(0);
			if (datatype.equalsIgnoreCase(type)) {
				sources = getNodeValues("source", flowNodes);
			}
		}
		return sources;

	}
	public static FoundationXML FmXMLParser(File file) throws Exception {
		FoundationXML fmXml = new FoundationXML();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		Node finalReport = getNodes("FinalReport", doc.getDocumentElement().getChildNodes()).get(0);
		NodeList childNodes = finalReport.getChildNodes();
		Node sample = getNodes("Sample", childNodes).get(0);
		NodeList sampleNodes = sample.getChildNodes();
		fmXml.setSampleId(getNodeValues("SampleId", sampleNodes).get(0));
		fmXml.setFmId(getNodeValues("FM_Id", sampleNodes).get(0));
		Node pmi = getNodes("PMI", childNodes).get(0);
		NodeList pmiNodes = pmi.getChildNodes();
		fmXml.setReportId(getNodeValues("ReportId", pmiNodes).get(0));
		fmXml.setMrn(getNodeValues("MRN", pmiNodes).get(0));
		fmXml.setDiagnosis(getNodeValues("SubmittedDiagnosis", pmiNodes).get(0));
		return fmXml;
	}
	private static List<Node> getNodes(String tagName, NodeList nodes) {
		List<Node> nodeList = new ArrayList<Node>();
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            nodeList.add(node);
	        }
	    }
	 
	    return nodeList;
	}
	 
	private static String getNodeValue(Node node) {
	    NodeList childNodes = node.getChildNodes();
	    for (int x = 0; x < childNodes.getLength(); x++ ) {
	        Node data = childNodes.item(x);
	        if (data.getNodeType() == Node.TEXT_NODE)
	            return data.getNodeValue();
	    }
	    return "";
	}
	 
	private static List<String> getNodeValues(String tagName, NodeList nodes) {
		List<String> values = new ArrayList<String>();
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    values.add(data.getNodeValue());
	            }
	        }
	    }
	    return values;
	}
	 
	private static String getNodeAttr(String attrName, Node node) {
	    NamedNodeMap attrs = node.getAttributes();
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	        Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    return "";
	}
	 
	private static String getNodeAttr(String tagName, String attrName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
	                    if ( data.getNodeName().equalsIgnoreCase(attrName) )
	                        return data.getNodeValue();
	                }
	            }
	        }
	    }
	 
	    return "";
	}
}	
