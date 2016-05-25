package org.mdacc.rists.bdi.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mdacc.rists.bdi.fm.models.FoundationXML;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser {
	public static void main(String[] args) throws Exception{
		File file = new File(args[0]);
		System.out.println(getAltPropName(file));
		
//		FmXMLParser(file);
//		readSourceXML(args[0], args[1]);
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
			String datatype = getNodeValue("datatype", flowNodes);
			if (datatype.equalsIgnoreCase(type)) {
				sources = getNodeValues("source", flowNodes);
			}
		}
		return sources;

	}
	public static String getAltPropName(File file) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		Node finalReport = getNode("FinalReport", doc.getDocumentElement().getChildNodes());
		Node genes = getNode("Genes", finalReport.getChildNodes());
		
		Node gene = getNode("Gene", genes.getChildNodes());
		NodeList geneList = gene.getChildNodes();
		String geneName = getNodeValue("Name", geneList);
//		Node gene = genes.getFirstChild();
		NodeList alterationList = getNode("Alterations", gene.getChildNodes()).getChildNodes();
		Node alt = getNode("Alteration", alterationList);
		Node altProps = getNode("AlterationProperties", alt.getChildNodes());
		Node prop = getNode("AlterationProperty", altProps.getChildNodes());
		String name = getNodeAttr("name", prop);
		return name;	
		
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
	
	public static Node getNode(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            return node;
	        }
	    }
	    return null;
	}
	public static List<Node> getNodes(String tagName, NodeList nodes) {
		List<Node> nodeList = new ArrayList<Node>();
	    for ( int i = 0; i < nodes.getLength(); i++ ) {
	    	if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
		        Node node = nodes.item(i);
		        if (node.getNodeName().equalsIgnoreCase(tagName)) {
		            nodeList.add(node);
		        }
	    	}
	    }
	 
	    return nodeList;
	}
	 
	public static String getNodeValue(Node node) {
	    NodeList childNodes = node.getChildNodes();
	    for (int x = 0; x < childNodes.getLength(); x++ ) {
	        Node data = childNodes.item(x);
	        if (data.getNodeType() == Node.TEXT_NODE)
	            return data.getNodeValue();
	    }
	    return "";
	}
	 
	public static String getNodeValue(String tagName, NodeList nodes ) {
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
	public static List<String> getNodeValues(String tagName, NodeList nodes ) {
		List<String> valueList = new ArrayList<String>();
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    valueList.add(data.getNodeValue());
	            }
	        }
	    }
	    return valueList;
	}
	 
	public static String getNodeAttr(String attrName, Node node) {
	    NamedNodeMap attrs = node.getAttributes();
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	        Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    return "";
	}
	 
	public static String getNodeAttr(String tagName, String attrName, NodeList nodes) {
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
