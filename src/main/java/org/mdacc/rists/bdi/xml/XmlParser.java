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
	public static List<WorkFlow> readXML(String xml) {
		try {
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			SAXParser parser = parserFactor.newSAXParser();
			SAXHandler handler = new SAXHandler();
			parser.parse(xml, handler);
			
			String cwd = System.getProperty("user.dir");
			int flowIndex = 0;
			for (WorkFlow flow : handler.flowList) {
				// check if fields are correct in xml
				if (flow.devEnv == null) {
					System.err.println("Development Environment not specified in " + xml);
					System.exit(1);
				}
				if (flow.type == null) {
					System.err.println("Data type not specified in flow " + Integer.toString(flowIndex));
					break;
				}
				
			}	
			return handler.flowList;
			
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
