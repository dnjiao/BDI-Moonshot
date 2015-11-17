package org.mdacc.rists.bdi.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class SAXHandler extends DefaultHandler {
	
	List<String> sourceList = null;
	List<WorkFlow> flowList = null;
	WorkFlow flow = null;
	boolean btype = false;
	boolean bsource = false;
	 
	@Override
	//Triggered when the start of tag is found.
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	                         throws SAXException {
		if (qName.equalsIgnoreCase("Flow")) {
			flow = new WorkFlow();
			sourceList = new ArrayList<String>();
			if (flowList == null) {
				flowList = new ArrayList<WorkFlow>();
			}
		} else if (qName.equalsIgnoreCase("datatype")) {
			btype = true;
		} else if (qName.equalsIgnoreCase("source")) {
			bsource = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("Flow")) {
			flow.setSources(sourceList);
			flowList.add(flow);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) 
	        throws SAXException {
		if (btype) {
			flow.setType(new String(ch, start, length));
			btype = false;
		} else if (bsource) {
			sourceList.add(new String(ch, start, length));
			bsource = false;
		}
	}
}
	

