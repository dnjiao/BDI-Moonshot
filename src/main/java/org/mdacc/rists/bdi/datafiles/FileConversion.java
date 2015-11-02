package org.mdacc.rists.bdi.datafiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileConversion {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: FileConvert [input_file_path] [type]");
			System.exit(1);
		}
		File in = new File(args[0]);
		if (!in.exists()) {
			System.err.println("ERROR: File " + args[0] + " does not exist.");
			System.exit(1);
		}
		String outpath = in.getParent() + "/" + FilenameUtils.removeExtension(in.getName()) + ".tsv";
		File out = new File(outpath);
		if (args[1].equalsIgnoreCase("flow")) {
			flowTsv(in, out);
		}
		else if (args[1].equalsIgnoreCase("immuno")) {
			immunoTsv(in, out);
		}
		else {
			System.err.println("ERROR: Invalid argument " + args[1]);
			System.exit(1);
		}
	}
	
	/**
	 * 
	 * @param in 
	 * @param out 
	 */
	
	/**
	 * convert flowcytometry result file from xls to tsv.
	 * @param in - flowcyto file in xls
	 * @param out - converted file in tsv
	 * @return 0 means failure, 1 means success
	 */
	public static int flowTsv(File in, File out) {
        StringBuffer buffer = new StringBuffer();
        try
        {
        	BufferedReader reader = new BufferedReader(new FileReader(in));
			PrintWriter writer = new PrintWriter(out);
			String line;
			int lineno = 0;
			
			String[] gateNames =new String[0];
			String[] gateValues = new String[0];
			String[] metainfo = new String[0];
			String accession = "";
			String panelName = "";
			while ((line = reader.readLine()) != null) {
				lineno++;
				String[] items = line.split(",");
				if (items.length < 5) {
					System.err.println("Invalid file format.");
					return 0;
				}
				if (lineno == 1) {
					gateNames = Arrays.copyOfRange(items, 4, items.length);
				}
				if (lineno == 2) {
					metainfo = parseSampleField(items[1]);
					accession = parsePanelField(items[2])[0];
					panelName = parsePanelField(items[2])[1];
					gateValues = Arrays.copyOfRange(items, 4, items.length);
					break;
				}
			}
			reader.close();
			
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
        
	}
	
	private static String[] parseSampleField(String string) {
		String[] tmp = string.split("-");
		String[] meta = new String[4];
		meta[0] = tmp[0] + "-" + tmp[1];
		meta[1] = tmp[2];
		meta[2] = tmp[3];
		meta[3] = tmp[4];
		return meta;
	}

	private static String[] parsePanelField(String string) {
		String[] tmp = string.split("-");
		return Arrays.copyOfRange(tmp, 0, 2);
	}
	

	/**
	 * convert immunopath result file from xls to tsv
	 * @param in - immunopath result file in xls or xlsx
	 * @param out - converted/transposed file in tsv
	 */
	public static void immunoTsv (File in, File out) {
		try {
			//FileOutputStream fos = new FileOutputStream(out);
			PrintWriter writer = new PrintWriter(out);
			Workbook workbook = null;
			Sheet sheet;
			// if xls format, use HSSF, if xlsx, use XSSF
			if (in.getName().endsWith(".xls")) {
				workbook = new HSSFWorkbook(new FileInputStream(in));
			}
			else if (in.getName().endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(new FileInputStream(in));
			}
			else {
				System.err.println("ERROR: " + in.getName() + " is not in xls/xlsx format.");
				System.exit(1);
			}
	        // Get the workbook object for XLS file
	        Cell cell;
	        Row row;
	        int readFlag = 0;
	        
	        String im, ct, norm, type, specimen, mrn, accession;
	        Cell cellIm, cellCt, cellNorm;
	        
	        // print title row
	        writer.println("Specimen_ID\t" + "MRN\t" + "Tissue_Acc\t" + "biomarker\t" + "type\t" + "im\t" + "ct\t" + "norm");
	        // loop thru sheets (type)
	        for (int i = 0; i < 3; i ++) {
	        	sheet = workbook.getSheetAt(i);
	        	type = sheet.getSheetName();
	        	List<String> markers = new ArrayList<String>();
	        	Iterator<Row> rowIterator = sheet.iterator();
	        	while (rowIterator.hasNext()) 
	        	{
		        	row = rowIterator.next();
		        	cell = row.getCell(0);
		        	if (cell != null) {
		        		int celltype = cell.getCellType();
		        		if (readFlag == 0) {
		        			// read block starts with "ID"
		        			if (celltype == Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase("ID")) {
		        				readFlag = 1;
		        				Iterator<Cell> cellIterator = row.cellIterator();
		        		        // get the list of biomarker names from first row of first sheet
		        		        int cellIndex = 0;
		        		        while (cellIterator.hasNext()) 
		        	            {
		        		        	cell = cellIterator.next();
		        		        	// get list of biomarkers from first row of every block
		        		        	if (cellIndex > 4 && !cell.getStringCellValue().equals("")) { // merged cell has two trailing ""
		        		        		markers.add(cell.getStringCellValue());
		        		        	}
		        		        	cellIndex ++;
		        		        		
		        	            }
		        		        // skip next row (due to merged cell)
		        		        row = rowIterator.next();
		        		        continue;
			        		}
		        		}
		        		if (readFlag == 1) {
		        			// read block stops at "Average"
		        			if (celltype == Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase("Average")) {
		        				readFlag = 0;
		        				markers = new ArrayList<String>();
		        			}
		        			else {
		        				// generate RIS specimen ID
		        				specimen = "RIS" + UUID.randomUUID().toString().replaceAll("-", "");
		        				mrn = row.getCell(1).getStringCellValue();
		        				accession = row.getCell(2).getStringCellValue();
		        				if (accession.lastIndexOf(' ') != (accession.length() - 1) && accession.lastIndexOf(' ') != -1) {
		        					accession = accession.substring(0, accession.lastIndexOf(' '));
		        				}
		        				if (StringUtils.countMatches(accession, "-") == 2) {
		        					accession = accession.substring(0, accession.lastIndexOf('-'));
		        				}
		        				
			        			// loop thru biomarkers		        		
			        			for (int j = 0; j < markers.size(); j ++) {
			        				
		    	        			cellIm = row.getCell(5 + j * 3);
		    	        			if (cellIm != null) {
		    	        				im = Double.toString(cellIm.getNumericCellValue());
		    	        			} 
		    	        			else {
		    	        				im = "";
		    	        			}
		    	        			cellCt = row.getCell(6 + j * 3);
		    	        			if (cellCt != null) {
		    	        				ct = Double.toString(cellCt.getNumericCellValue());
		    	        			} 
		    	        			else {
		    	        				ct = "";
		    	        			}
		    	        			cellNorm = row.getCell(7 + j * 3);
		    	        			if (cellNorm != null) {
		    	        				norm = Double.toString(cellNorm.getNumericCellValue());
		    	        			} 
		    	        			else {
		    	        				norm = "";
		    	        			}
		    	        			writer.println(specimen + "\t" + mrn + "\t" + accession + "\t" + markers.get(j) + "\t" + type + "\t" + im + "\t" + ct + "\t" + norm);
			    	        	}
		        			}
		        		}
		        	
		        	}
		        }
	        
            }
	        writer.close();
	        System.out.println("File conversion complete. " + out.getAbsolutePath());
	        
		} catch (FileNotFoundException e) {
            e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
}