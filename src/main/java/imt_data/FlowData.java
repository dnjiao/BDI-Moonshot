package imt_data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;


public class FlowData {
	public static void BdiFlowSummary(File dir, String outFile) {
		try {
			List<File> dataFiles = getDataFiles(dir);
			File out = new File(outFile);
			PrintWriter writer = new PrintWriter(new FileOutputStream(out), true);
			for (File f : dataFiles) {
				processSingleFile(f, writer);
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
	private static List<File> getDataFiles(File dir) {
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
	 */
	private static void processSingleFile(File file, PrintWriter writer) {
		FileInputStream fis = new FileInputStream(file);
		// create a workbook from input data excel file
		HSSFWorkbook workbook = new HSSFWorkbook(fis);
		// get the first sheet from data file
		HSSFSheet sheet = workbook.getSheetAt(0);
		String name = file.getName().substring(0, file.getName().length() - 4).split("_", 2)[1];
		
		Iterator<Row> rowIter = sheet.rowIterator();
		int rowCount = 0;
		List<Integer> comList = new ArrayList<Integer>();
		List<Integer> isoList = new ArrayList<Integer>();
		List<Integer> nonList = new ArrayList<Integer>();
		HashMap<String, Integer> gateList = null;
		// count the rows that have "com" keyword
		while (rowIter.hasNext()) {
			Row row = rowIter.next();
			if (rowCount == 0) {
				gateList = getGateList(row);
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
				readDataRow(sheet.getRow(j), gateList);
			}
		}
		workbook.close();
	}

	/**
	 * Read first row of data sheet and get the list of gate names and their corresponding column indexes. 
	 * @param row - first row of sheet
	 * @return - list of gate names
	 */
	private static HashMap<String, Integer> getGateList(Row row) {
		Iterator<Cell> cellIter = row.cellIterator();
		Cell cell;
		int cellCount = 0;
		HashMap<String, Integer> gateList = new HashMap<String, Integer>();
		// read data sheet and record column names and their indexes
		while (cellIter.hasNext()) {
			cell = cellIter.next();
			cellCount ++;
			if (cellCount > 3) {
				gateList.put(cell.getStringCellValue(), cellCount - 1);
			}
		}
		return gateList;
	}


}