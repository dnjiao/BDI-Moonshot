package org.mdacc.rists.bdi.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

public class TransferUtils {
	public static void main(String[] args) throws IOException {
		File file1 = new File("/Users/djiao/Work/moonshot/data/dev/mapping/MSBIO_SPCMN_20150812_11232015162155.txt");
		File file2 = new File("/Users/djiao/Work/moonshot/data/dev/mapping/MSBIO_SPCMN_20150813_11232015162155.txt");
		
		System.out.println(FileUtils.contentEquals(file1, file2));
	}
	
	/**
	 * determine if a file is mapping file based on first line text
	 * @param file - input file
	 * @return - boolean, true means mapping false means not
	 */
	public static boolean isMapping(File file) {
		boolean bool = false;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String firstline = reader.readLine();
			if (firstline.startsWith("Project|Subproject")) {
				bool = true;
			}
			else
				bool = false;
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bool;
	}

	public static String convertTypeStr(String type) {
		switch (type) {
			case "vcf":
				return "VCF";
			case "cnv":
				return "CNV";
			case "immunopath":
				return "Immunopathology";
			case "flowcyto":
				return "Flow Cytometry";
			case "mapping":
				return "MRN Mapping";
			case "gene":
				return "RNASeq Gene Counts";
			case "exon":
				return "RNASeq Exon Counts";
			case "junction":
				return "RNASeq Junction Counts";
			default:
				System.err.println("Invalid file type: " + type);
				return null;
		}
	}
	/**
	 * determine if a file is the type
	 * @param filename 
	 * @param type - file type, e.g. "vcf"
	 * @return true if a file is the type, false otherwise
	 */
	public static boolean isType(String filename, String type) {
		if (type.equalsIgnoreCase("vcf")) {
			if(filename.endsWith(".vcf")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("cnv")) {
			if (filename.contains(".segList") && filename.endsWith(".tsv")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("gene")) {
			if (filename.contains(".gene.") && filename.endsWith(".tsv")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("exon")) {
			if (filename.contains(".exon.") && filename.endsWith(".tsv")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("junction")) {
			if (filename.contains(".junctions.") && filename.endsWith(".txt")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("immunopath")) {
			if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("flowcyto")) {
			if (filename.endsWith(".csv") && filename.contains("moonshot")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * construct Linux command for file transfer
	 * @param protocol - command for transfer: cp/ln
	 * @param from - source path
	 * @param to - destination path
	 * @return - constructed command
	 */
	public static String cmdConstructor(String protocol, String from, String to) {
		if (protocol.equals("ln")) {
			return "ln -s " + from + " " + to;
		}
		else if (protocol.equals("cp")) {
			return "cp " + from + " " + to;
		}
		else
			return null;
	}
	
	/**
	 * rename file with new extension
	 * @param filename - old file name
	 * @param ext - new extension
	 * @return
	 */
	protected static String switchExt(String filename, String ext) {
		int stop = filename.lastIndexOf(".");
		String base = filename.substring(0, stop);
		return base + "." + ext;
	}

	
	public static void removeReturnChar(File oldfile, File newfile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(oldfile));
			PrintWriter writer = new PrintWriter(newfile);
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("\r")) {
					line = line.replaceAll("\r\n", "\n");
					line = line.replaceAll("\r", "");
				}
				writer.println(line);
			}
			writer.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}