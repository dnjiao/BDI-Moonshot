package org.mdacc.rists.bdi.xml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser {
	public static void main(String[] args) {
	}
	public static List<String> readXML(String xml, String type) {
		try {
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			SAXParser parser = parserFactor.newSAXParser();
			SAXHandler handler = new SAXHandler();
			parser.parse(xml, handler);
					
			for (WorkFlow flow : handler.flowList) {
				// check if fields are correct in xml
				if (flow.type.equalsIgnoreCase(type)) {
					return flow.sources;
				}
				
			}				
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;	

	}
}	
