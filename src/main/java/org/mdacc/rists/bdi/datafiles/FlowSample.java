package org.mdacc.rists.bdi.datafiles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FlowSample {
	String mrn;
	String protocol;
	int accession;
	String collection;
	String panel;
	String dateStr;
	List<FlowGate> gates;
	
	public String getMrn() {
		return mrn;
	}
	public void setMrn(String mrn) {
		this.mrn = mrn;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public int getAccession() {
		return accession;
	}
	public void setAccession(int accession) {
		this.accession = accession;
	}
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public String getPanel() {
		return panel;
	}
	public void setPanel(String panel) {
		this.panel = panel;
	}
	public String getDateStr() {
		return dateStr;
	}
	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}
	public List<FlowGate> getGates() {
		return gates;
	}
	public void setGates(List<FlowGate> gates) {
		this.gates = gates;
	}
	
	public void readSampleFile(File file) {
		try {
			String filename = file.getName();
			this.panel = filename.split("moonshot ")[1].split(".csv")[0];
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			String[] gateArray = line.split(",");
			if (gateArray.length > 4) {
				for (int i = 4; i < gateArray.length; i++) {
					FlowGate gate = new FlowGate();
					gate.setName(gateArray[i]);
					gates.add(gate);
				}
				line = reader.readLine();
				String[] valueArray = line.split(",");
				if (valueArray.length == gateArray.length) {
					String sampleName = valueArray[1];
					String[] fields = sampleName.split("-");
					if (fields.length > 4) {
						this.protocol = fields[0] + "-" + fields[1];
						this.accession = Integer.parseInt(fields[2]);
						this.collection = fields[3];
						this.dateStr = fields[4];
					}
					int i = 4;
					for (FlowGate g : gates) {
						g.setValue(Double.parseDouble(valueArray[i]));
						i++;
					}
					
				}
				else {
					System.err.println("Incorrect file content");
				}
			}
			else {
				System.err.println("Incorrect file content");
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToTsv(File file) {
		try {
			PrintWriter out = new PrintWriter(file);
			for (FlowGate gate : this.gates) {
				String line = this.protocol + "\t" + this.accession + "\t" + this.panel + "\t" + 
						this.collection + "\t" + gate.getName() + "\t" + gate.getValue();
				out.println(line);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}