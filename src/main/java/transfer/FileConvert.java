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

public class FileConvert {
	public static void flowTsv(File in, File out) {
		 // For storing data into CSV files
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
	                while (cellIterator.hasNext()) 
	                {
	                        cell = cellIterator.next();
	                        
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
	
	public static void immunoTsv (File in, File out) {
		try {
			//FileOutputStream fos = new FileOutputStream(out);
			PrintWriter writer = new PrintWriter(out);
			
	        // Get the workbook object for XLS file
	        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(in));
	        HSSFSheet sheet;
	        HSSFSheet sheet0 = workbook.getSheetAt(0);
	        
	        Cell cell;
	        Row row0, row;
	        String type;
	        List<String> markers = new ArrayList<String>();
	        row0 = sheet0.getRow(0);
	        Iterator<Cell> cellIterator = row0.cellIterator();
	        
	        // get the list of biomarker names from first row of first sheet
	        int cellcount = 0;
	        while (cellIterator.hasNext()) 
            {
	        	cell = cellIterator.next();
	        	if (cellcount > 2) {
	        		markers.add(cell.getStringCellValue());
	        	}
	        	cellcount ++;
	        		
            }
	        
	        // get list of specimen from third column of first sheet
	        List<String> samples = new ArrayList<String>();
	        int rowcount = 0;
	        Iterator<Row> rowIterator = sheet0.iterator();
	        while (rowIterator.hasNext()) 
	        {
	        	row = rowIterator.next();
	        	if (rowcount > 1) {
		        	cell = row.getCell(2);
		        	samples.add(cell.getStringCellValue());
		        	
	        	}
	        	rowcount ++;
	        
            }
	        
	        String im, ct, norm;
	        Cell cellIm, cellCt, cellNorm;
	        
	        // print title row
	        writer.println("biomarker" + "\t" + "type" + "\t" + "specimen" + "\t" + "im" + "\t" + "ct" + "\t" + "norm");
	        // loop thru sheets (type)
	        for (int i = 0; i < 3; i ++) {
	        	sheet = workbook.getSheetAt(i);
	        	type = sheet.getSheetName();
	        	// loop thru columns (biomarker)
        		for (int j = 0; j < markers.size(); j ++) {
        			// loop thru rows (sample)
        			for (int k = 0; k < samples.size(); k ++) {
        				row = sheet.getRow(k + 2);
	        			cellIm = row.getCell(3 + j * 3);
	        			if (cellIm != null) {
	        				im = Double.toString(cellIm.getNumericCellValue());
	        			} 
	        			else {
	        				im = "";
	        			}
	        			cellCt = row.getCell(4 + j * 3);
	        			if (cellCt != null) {
	        				ct = Double.toString(cellCt.getNumericCellValue());
	        			} 
	        			else {
	        				ct = "";
	        			}
	        			cellNorm = row.getCell(5 + j * 3);
	        			if (cellNorm != null) {
	        				norm = Double.toString(cellNorm.getNumericCellValue());
	        			} 
	        			else {
	        				norm = "";
	        			}
	        			writer.println(markers.get(j) + "\t" + type + "\t" + samples.get(k) + "\t" + im + "\t" + ct + "\t" + norm);
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