package imt_data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import db_op.OracleDB;


public class FlowData {
	
	public static void main(String[] args) {
		File dir = new File(args[0]);
		BdiFlowSummary(dir, args[1]);
	}
	
	public static void BdiFlowSummary(File dir, String outFile) {
		try {
			// get connection to Oracle DB
			Connection con = OracleDB.getConnection();
			List<File> dataFiles = getDataFiles(dir);
			File out = new File(outFile);
			PrintWriter writer = new PrintWriter(new FileOutputStream(out), true);
			for (File f : dataFiles) {
				processSingleFile(f, writer, con);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get the list of flowcyto data files
	 * @param dir - source diretory that contains data files
	 * @return - list of found files
	 */
	public static List<File> getDataFiles(File dir) {
		List<File> dataFiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (File file : files) {	
			if (file.isDirectory() == false ) {
				if (file.getName().endsWith(".xls")){
					dataFiles.add(file);
				}
			}
		}
		return dataFiles;
	}
	
	/**
	 * Process single flowcyto data file
	 * @param file - Input data file (xls)
	 * @param writer - PrintWriter for output file (tsv)
	 * @param con - connection to Database for panel/gate look-up
	 */
	public static void processSingleFile(File file, PrintWriter writer, Connection con) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
		
			// create a workbook from input data excel file
			HSSFWorkbook workbook;
			workbook = new HSSFWorkbook(fis);
			
			// get the first sheet from data file
			HSSFSheet sheet = workbook.getSheetAt(0);
			String filename = file.getName().substring(0, file.getName().length() - 4).split("_", 2)[1];
			Statement stmt;
			stmt = con.createStatement();
			
			String query = "select ROW_ID, CODE, NAME, ANTIBODIES from PANEL_TB where FILE_NAME = '" + filename + "'";
			ResultSet rs = stmt.executeQuery(query);
			// if panel does not exist in database, skip the file
			if (rs.next() == false) {
				System.out.println(filename + " does not have a matching Panel in Database.");
				return;
			}
			int panelID = rs.getInt("ROW_ID");
			String panelCode = rs.getString("CODE");
			String panelName = rs.getString("NAME");
			String panelAntibodies = rs.getString("ANTIBODIES");
			
			Iterator<Row> rowIter = sheet.rowIterator();
			int rowCount = 0;
			List<Integer> comList = new ArrayList<Integer>();
			List<Integer> isoList = new ArrayList<Integer>();
			List<Integer> nonList = new ArrayList<Integer>();
			List<FlowGate> gateList = null;
			// count the rows that have "com" keyword
			while (rowIter.hasNext()) {
				Row row = rowIter.next();
				if (rowCount == 0) {
					gateList = getGateList(row, panelID, con);
				}
				rowCount ++;
				if (row.getCell(0) != null && row.getCell(0).getStringCellValue().toLowerCase().contains("mean")) {
					break;
				}
				String cell2 = row.getCell(2).getStringCellValue().toLowerCase();
				if (rowCount != 1) {
					if (cell2.contains("com")) {
						comList.add(row.getRowNum());
					}
					else if (cell2.contains("iso")) {
						isoList.add(row.getRowNum());
					}
					else {
						nonList.add(row.getRowNum());
					}
				}
			}
			if (comList.size() == 0 && nonList.size() != 0) {
				comList.clear();
				comList = nonList;
			}
			
			if (comList.size() != 0) {  // no "com" specified in column "Staining", all rows are accounted format
				for (int j : comList) {
					FlowSample samp = readDataRow(sheet.getRow(j), gateList);
					for (FlowGate gate : samp.getGates()) {
						String[] words = {samp.getSpecimenID(), panelCode, panelName, panelAntibodies,
					  		  			  gate.getCode(), gate.getName(), gate.getDefinition(), Double.toString(gate.getValue()), gate.getParent()};
						writer.println(StringUtils.join(words, "\t"));
					}
				}
			}
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Read first row of data sheet and get the list of gate names and their corresponding column indexes. 
	 * @param row - first row of sheet
	 * @param panelID - panel ID in Panel table
	 * @param con - connection to Oracle DB
	 * @return - list of FlowGate objects
	 */
	public static List<FlowGate> getGateList(Row row, int panelID, Connection con) {
		List<FlowGate> gateList = new ArrayList<FlowGate>();
		Iterator<Cell> cellIter = row.cellIterator();
		Cell cell;
		int cellCount = 0;
		HashMap<String, Integer> gateMap = new HashMap<String, Integer>();
		// read data sheet and record column names and their indexes
		while (cellIter.hasNext()) {
			cell = cellIter.next();
			cellCount ++;
			if (cellCount > 3) {
				gateMap.put(cell.getStringCellValue(), cellCount - 1);
			}
		}
		
		// knock out the gatenames that do not exist in Gate table in DB
		try {
			Statement stmt = con.createStatement();
			String query = "select FILE_COL_NAME, CODE, DEFINITION, PARENT_GATE from GATE_TB where PANEL_ID = " + Integer.toString(panelID);
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				for (String key : gateMap.keySet()) {
					int foundFlag = 0;
					if (key.equalsIgnoreCase(rs.getString("FILE_COL_NAME"))) {
						foundFlag = 1;
						String gateName = rs.getString("CODE") + " (" + rs.getString("DEFINITION") + ")";
						FlowGate gate = new FlowGate(gateName, rs.getString("CODE"), 
								rs.getString("DEFINITION"), rs.getString("PARENT_GATE"), gateMap.get(key));
						gateList.add(gate);
						gateMap.remove(key);
						break;
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return gateList;
	}


	public static FlowSample readDataRow(HSSFRow row, List<FlowGate> gateList) {
		String sampleString = row.getCell(1).getStringCellValue();
		String[] fields = parseSampleField(sampleString);
		if (fields[1] == null) {
			System.err.println("Error with Sample field: " + sampleString);
			return null;
		}
		String date = fields[0];
		String mrn = fields[1].substring(3);
		String protocol = fields[2].split("-")[0] + "-" + fields[2].split("-")[1];
		String accession = fields[2].split("-")[2];
		String cycle = fields[3];
		String specimen = null;
		// if all information is available, construct specimen ID with them
		if (protocol != null && accession != null && cycle != null) {
			int collection = cycleToColl(fields[3]);
			specimen = protocol + ":" + accession + ":" + Integer.toString(collection);
		}
		
		// if any information is missing (most likely collection) generate RIS specimen ID with UID
		else {
			specimen = "RIS-" + UUID.randomUUID().toString().replaceAll("-", "");
		}
		
		for (FlowGate gate : gateList) {
			gate.setValue(row.getCell(gate.getCol()).getNumericCellValue());
		}
		
		FlowSample sample = new FlowSample(mrn, specimen, date, gateList);
		return sample;
	}
	
	/**
	 * Parse sample field and convert to String array
	 * @param str - sample field content, 2nd column in data xls
	 * @return - 4-member String array
	 */
	public static String[] parseSampleField(String str) {
		String[] out = new String[4];
		
		// split with one or more spaces
		String[] fields = str.split(" +");
		if (fields.length < 4) {
			System.out.println("ERROR: Invalid Sample Name: " + str);
			System.exit(1);
		}
		for (String s : fields) {
			if (isDateStr(s)) {
				out[0] = s;
			}
			if (isMrnStr(s)) {
				out[1] = s;
			}
			if (isProtocolStr(s)) {
				out[2] = s;
			}
			if (isCycleStr(s)) {
				if (out[3] == null)
					out[3] = s;
			}
		}
		return out;
	}
	
	public static boolean isDateStr(String str) {
		if (str.length() > 7 && str.length() < 11 && str.indexOf("-") == 4 && 
				StringUtils.countMatches(str, "-") == 2 &&
				Character.isDigit(str.charAt(0)) && 
				Character.isDigit(str.charAt(str.length() - 1))) 
			return true;
		return false;
	}

	public static boolean isMrnStr(String str) {
		if (str.startsWith("MRN"))
			return true;
		return false;
	}
	
	public  static boolean isProtocolStr(String str) {
		if (str.startsWith("PA") && StringUtils.countMatches(str, "-") == 3)
			return true;
		return false;
	}

	public static boolean isCycleStr(String str) {
		if (str.charAt(0) == 'C' && str.replaceAll("[^a-zA-Z]+", "").equals("CD") && (str.indexOf('D') - str.indexOf('C')) > 1)
			return true;
		return false;
	}
	
	/**
	 * Translate cycle "CxDy" format to collection number
	 * @param str - cycle string
	 * @return - collection number
	 */
	private static int cycleToColl(String str) {
		int dIndex = str.indexOf('D');
		int c = Integer.parseInt(str.substring(1, dIndex));
		int d = Integer.parseInt(str.substring(dIndex + 1));
		return (c - 1) * 2 + d;
	}
}