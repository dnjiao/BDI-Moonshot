package org.mdacc.rists.bdi.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mdacc.rists.bdi.models.FoundationXML;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ParseXMLFile {
	public static void main(String[] args) throws Exception{
		File file = new File(args[0]);
		FmXMLParser(file);
	}
	private static FoundationXML FmXMLParser(File file) throws Exception {
		FoundationXML fmXml = new FoundationXML();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		Node finalReport = getNode("FinalReport", doc.getDocumentElement().getChildNodes());
		NodeList childNodes = finalReport.getChildNodes();
		Node sample = getNode("Sample", childNodes);
		NodeList sampleNodes = sample.getChildNodes();
		fmXml.setSampleId(getNodeValue("SampleId", sampleNodes));
		fmXml.setFmId(getNodeValue("FM_Id", sampleNodes));
		Node pmi = getNode("PMI", childNodes);
		NodeList pmiNodes = pmi.getChildNodes();
		fmXml.setReportId(getNodeValue("ReportId", pmiNodes));
		fmXml.setMrn(getNodeValue("MRN", pmiNodes));
		fmXml.setDiagnosis(getNodeValue("SubmittedDiagnosis", pmiNodes));
		return fmXml;
	}
	private static Node getNode(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            return node;
	        }
	    }
	 
	    return null;
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
	 
	private static String getNodeValue(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    return data.getNodeValue();
	            }
	        }
	    }
	    return "";
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
