package transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileConvert {
	/**
	 * convert flowcytometry result file from xls to tsv.
	 * @param in - flowcyto file in xls
	 * @param out - converted file in tsv
	 */
	public static void flowTsv(File in, File out) {
        StringBuffer buffer = new StringBuffer();
        try 
        {
	        FileOutputStream fos = new FileOutputStream(out);
	
	        // Get the workbook object for XLS file
	        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(in));
	        // Get first sheet from the workbook
	        HSSFSheet sheet = workbook.getSheetAt(0);
	        Cell cell;
	        Row row;
	
	        // Iterate through each rows from first sheet
	        Iterator<Row> rowIterator = sheet.iterator();
	        while (rowIterator.hasNext()) 
	        {
	                row = rowIterator.next();
	                // For each row, iterate through each columns
	                Iterator<Cell> cellIterator = row.cellIterator();
	                int cellIndex = 0;
	                while (cellIterator.hasNext()) 
	                {
	                	cellIndex ++;
                        cell = cellIterator.next();
                        if (cellIndex > 1 && cellIndex < 5) { // omit three cols about specimen
	                        switch (cell.getCellType()) 
	                        {
		                        case Cell.CELL_TYPE_BOOLEAN:
		                                buffer.append(cell.getBooleanCellValue() + "\t");
		                                break;
		                                
		                        case Cell.CELL_TYPE_NUMERIC:
		                                buffer.append(cell.getNumericCellValue() + "\t");
		                                break;
		                                
		                        case Cell.CELL_TYPE_STRING:
		                                buffer.append(cell.getStringCellValue() + "\t");
		                                break;
		
		                        case Cell.CELL_TYPE_BLANK:
		                                buffer.append("" + "\t");
		                                break;
		                        
		                        default:
		                                buffer.append(cell + "\t");
	                        }
                        }
	                        
	                }
	                buffer.append("\n"); 
	        }
	
	        fos.write(buffer.toString().getBytes());
	        fos.close();
	        workbook.close();
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
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
	        
	        String im, ct, norm, type, accession;
	        Cell cellIm, cellCt, cellNorm;
	        
	        // print title row
	        writer.println("biomarker" + "\t" + "type" + "\t" + "accession" + "\t" + "im" + "\t" + "ct" + "\t" + "norm");
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
		        			if (celltype == Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase("Average")) {
		        				readFlag = 0;
		        				markers = new ArrayList<String>();
		        			}
		        			else {
			        			// loop thru biomarkers		        		
			        			for (int j = 0; j < markers.size(); j ++) {
			        				accession = row.getCell(2).getStringCellValue();
			        				// remove "#x" from accession 
			        				if (accession.contains("#")) {
			        					accession = accession.substring(0, accession.lastIndexOf("#"));
			        				}
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
		    	        			writer.println(markers.get(j) + "\t" + type + "\t" + accession + "\t" + im + "\t" + ct + "\t" + norm);
			    	        	}
		        			}
		        		}
		        	
		        	}
		        }
	        
            }
	        writer.close();
	        
		} catch (FileNotFoundException e) {
            e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
}