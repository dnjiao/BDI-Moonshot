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
		try {
			SAXParserFactory parserFactor = SAXParserFactory.newInstance();
			SAXParser parser = parserFactor.newSAXParser();
			SAXHandler handler = new SAXHandler();
			parser.parse(args[0], handler);
			
			// check if fields are correct in xml
			if (handler.flow.devEnv == null) {
				System.err.println("Development Environment not specified in " + args[0]);
				System.exit(1);
			}
			if (handler.flow.type == null) {
				System.err.println("Data type not specified in " + args[0]);
				System.exit(1);
			}
			if (handler.flow.sources.size() == 0) {
				System.err.println("Sources missing in " + args[0]);
				System.exit(1);
			}
			System.out.println(handler.flow.devEnv);
			System.out.println(handler.flow.type);
			// write source dirs to file source_list
			String cwd = System.getProperty("user.dir");
			System.out.println(cwd);
			File sourceList = new File(cwd, "source_list");
			PrintWriter writer = new PrintWriter(sourceList);
			
			for (String s : handler.flow.sources) {
				writer.println(s);
				System.out.println(s);
			}
			writer.close();
			
			// set PPM environment variables
//			Runtime.getRuntime().exec("ppmsetvar -f DEV_ENV=" + handler.flow.devEnv);
//			Runtime.getRuntime().exec("ppmsetvar -f TYPE=" + handler.flow.type);
//			Runtime.getRuntime().exec("ppmsetvar -f SOURCES=source_list");
			
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		

	}
}	
class SAXHandler extends DefaultHandler {

	  List<String> dirs = new ArrayList<String>();
	  List<WorkFlow> flows = new ArrayList<WorkFlow>();
	  WorkFlow flow = null;
	  String content;
	  boolean benv = false;
	  boolean btype = false;
	  boolean bsource = false;
	 
	  @Override
	  //Triggered when the start of tag is found.
	  public void startElement(String uri, String localName, String qName, Attributes attributes) 
	                           throws SAXException {
		  if (qName.equalsIgnoreCase("devenv")) {
			  benv = true;
		  }
		  if (qName.equalsIgnoreCase("datatype")) {
			  btype = true;
			  flow = new WorkFlow();
		  }
		  if (qName.equalsIgnoreCase("source")) {
			  bsource = true;
		  }
	  }

	  @Override
	  public void endElement(String uri, String localName, String qName) throws SAXException {
		  if (qName.equalsIgnoreCase("devenv")) {
			  flow.devEnv = content;
		  }
		  if (qName.equalsIgnoreCase("datatype")) {
			  flow.type = content;
		  }
		  if (qName.equalsIgnoreCase("source")) {
			  flow.sources.add(content);
		  }
	  }

	  @Override
	  public void characters(char[] ch, int start, int length) 
	          throws SAXException {
		  content = String.copyValueOf(ch, start, length).trim();
	  }

	}
	

