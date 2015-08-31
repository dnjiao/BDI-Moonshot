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

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import dao.OracleDB;


public class FlowData {
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
		String prefix = dir.getName().split(" ")[0];
		for (File file : files) {	
			if (file.isDirectory() == false ) {
				if (file.getName().startsWith(prefix) && file.getName().endsWith(".xls")){
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
			
			String query = "select ID, PANEL_CODE, PANEL_NAME, PANEL_ANTIBODIES from IMT_PANEL where FILENAME = " + filename;
			ResultSet rs = stmt.executeQuery(query);
			// if panel does not exist in database, skip the file
			if (rs.next() == false) {
				System.out.println(filename + " does not have a matching Panel in Database.");
				return;
			}
			int panelID = rs.getInt("ID");
			String panelCode = rs.getString("PANEL_CODE");
			String panelName = rs.getString("PANEL_NAME");
			String panelAntibodies = rs.getString("PANEL_ANTIBODIES");
			
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
			
			if (comList.size() != 0) {  // no "com" specified in column "Staining", all rows are accounted for
				for (int j : comList) {
					readDataRow(sheet.getRow(j), gateList, writer);
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
			String query = "select GATE_COLNAME, GATE_CODE, GATE_NAME, GATE_DEF, PARENT_GATE from IMT_GATE where PANEL_ID = " + Integer.toString(panelID);
			ResultSet rs = stmt.executeQuery(query);
			
			for (String key : gateMap.keySet()) {
				int foundFlag = 0;
				rs.first();
				while (rs.next()) {
					if (key.equalsIgnoreCase(rs.getString("GATE_COLNAME"))) {
						foundFlag = 1;
						FlowGate gate = new FlowGate(rs.getString("GATE_NAME"), rs.getString("GATE_CODE"), 
								rs.getString("GATE_DEF"), rs.getString("PARENT_GATE"), gateMap.get(key));
						gateList.add(gate);
						break;
					}
				}
				// does not find gate in DB
				if (foundFlag == 0) {
					gateMap.remove(key);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return gateList;
	}


	public static List<FlowGate> readDataRow(HSSFRow row, List<FlowGate> gateList, PrintWriter writer) {
		
		return null;
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
}