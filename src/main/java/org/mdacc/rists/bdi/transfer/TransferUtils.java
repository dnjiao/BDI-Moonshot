package org.mdacc.rists.bdi.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TransferUtils {
	public static void main(String[] args) throws IOException, URISyntaxException {
		//LAB02-152 Summary.xlsx
		//2009-0135, 2009-0322, 2005-0027, 2006-0080 Summary.xlsx
		File file1 = new File("/Users/djiao/Work/moonshot/immunopath/Melanoma FRZ Summary.xlsx");
		File file2 = new File("/Users/djiao/Work/moonshot/immunopath/Melanoma FRZ Summary.txt");
		immunoTsv(file1, file2);
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
			case "splice":
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
		if (filename.startsWith(".")) {
			return false;
		}
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

	/**
	 * fix format of mapping files including bad return symbols and missing specimen IDs
	 * @param oldfile - old file
	 * @param newfile - new file
	 */
	public static void fixMappingFile(File oldfile, File newfile) {
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

	/**
	 * convert flowcytometry result file from xls to tsv.
	 * @param in - flowcyto file in xls
	 * @param out - converted file in tsv
	 * @return 0 means failure, 1 means success
	 */
	public static int flowTsv(File in, File out) {
	    try
	    {
	    	BufferedReader reader = new BufferedReader(new FileReader(in));
			PrintWriter writer = new PrintWriter(out);
			String line;
			int lineno = 0;
			
			String[] gateNames =new String[0];
			String[] gateValues = new String[0];
			String[] metainfo = new String[0];
			String panelName = "";
			String specimen = "";
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
					panelName = parsePanelField(items[2])[2];
					gateValues = Arrays.copyOfRange(items, 4, items.length);
					
					writer.println("Accession|PanelName|Protocol|Tumor|Date|GateName|GateValue");
					for (int i=0; i < gateNames.length; i++) {
						writer.println(metainfo[1] + "|" + panelName + "|" + 
								metainfo[0] + "|" + metainfo[2] + "|" + metainfo[3] + "|" + gateNames[i] + "|" + gateValues[i]);
					}
					break;
				}
			}
			reader.close();
			writer.close();
			
	    } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return 0;
	    } catch (IOException e) {
	            e.printStackTrace();
	            return 0;
	    }
	    return 1;
	    
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
		return Arrays.copyOfRange(tmp, 0, 3);
	}

	/**
	 * convert immunopath result file from xls to tsv
	 * @param in - immunopath result file in xls or xlsx
	 * @param out - converted/transposed file in tsv
	 */
	public static int immunoTsv (File in, File out) {
		try {
			// flag for deletion of output file: 0 delete, 1 keep.
			int deleteFlag = 0;
			//FileOutputStream fos = new FileOutputStream(out);
			PrintWriter writer = new PrintWriter(out);
			Workbook workbook = null;
			Sheet sheet;
			// if xls format, use HSSF, if xlsx, use XSSF
			try {
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
			} catch (OutOfMemoryError e) {
				System.out.println(in.getAbsolutePath() + " Out of memory loading spreadsheet.");
				out.delete();
				return 0;
			}
	        // Get the workbook object for XLS file
			if (workbook.getNumberOfSheets() < 3) {
				System.out.println(in.getAbsolutePath() + " Invalid number of sheets.");
				out.delete();
				return 0;
			}
	        // print title row
	        writer.println("Protocol|MRN|Tissue_Acc|biomarker|type|IM|CT|N|TZ");
	        // loop thru sheets (type: Density, Percent, H-Score)
	        for (int i = 0; i < 3; i ++) {
	        	// flag for reading block: 0 no read; 1 first row of title row; 2 start reading values
	        	int readFlag = 0;	        	
	        	String protocol = "";
		        String mrn = "";
		        String accession = "";
	        	sheet = workbook.getSheetAt(i);
	        	// remove merged cells (only backward works)
	        	for(int r=sheet.getNumMergedRegions() - 1; r >= 0; r--)
	        	{
	        	    sheet.removeMergedRegion(r);	        	    
	        	}
	        	String type = sheet.getSheetName();
	        	
	        	int mrnIndex = -1;
				int tissueAccIndex = -1;
				int protocolAccIndex = -1;
				// map of marker names (CD3, CD4, ..)
        		Map<String, Integer> markerMap = null;
        		// map of attributes (IM, CT, N, TZ)
        		Map<Integer, String> attributeMap = null;
        		for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++)
	        	{
        			Row row = sheet.getRow(rowIndex);
        			if (readFlag == 0) {
	        			if (row == null) {
	        				continue;
	        			}
			        	Cell cell = row.getCell(0);
			        	if (cell != null) {
			        		int celltype = cell.getCellType();
		        			// read block starts with "ID", parse 1st row to get column names
		        			if (celltype == Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase("ID")) {
		        				// get protocol # from 2 rows before
		        				if (rowIndex > 2) {
		        					Row protocolRow = sheet.getRow(rowIndex - 2);
		        					if (protocolRow != null) {
			        					Cell protocolCell = protocolRow.getCell(0);
			        					// extract protocol number
			        					if (protocolCell != null)
			        					{
			        						String protocolStr = protocolRow.getCell(0).getStringCellValue();
			        						String[] splits = protocolStr.split(" ");
			        						for (String split : splits) {
			        							if (split.contains("-") && StringUtils.countMatches(split, "-") == 1) {
			        								protocol = split;
			        								break;
			        							}
			        						}
	
			        					}
		        					}
		        				}
		        				Row firstRow = row;
		        				rowIndex ++;
		        				Row secondRow = sheet.getRow(rowIndex);
		        				readFlag = 1;
		        				deleteFlag = 1;
		        				mrnIndex = -1;
		        				tissueAccIndex = -1;
		        				protocolAccIndex = -1;
	
		        				markerMap = new LinkedHashMap<String, Integer>();
		        				attributeMap = new LinkedHashMap<Integer, String>();
		        		        // get the list of biomarker names from first row
		        		        int colIndex = 0;
		        		        for (Cell ce : firstRow)
		        	            {
		        		        	// if cell is not empty
		        		        	if (ce != null) {	        		        		
		        		        		String cellStr = ce.getStringCellValue().toLowerCase();
		        		        		if (cellStr.startsWith("tissue acc")) {
		        		        			tissueAccIndex = colIndex;
		        		        		}
		        		        		else if(cellStr.equals("mrn")) {
		        		        			mrnIndex = colIndex;
		        		        		}
		        		        		else if(cellStr.startsWith("protocol acc")) {
		        		        			protocolAccIndex = colIndex;
		        		        		}
		        		        		// get mapping between marker names and column index in the sheet
		        		        		else if (!cellStr.equals("")) {
		        		        			String secondStr = secondRow.getCell(colIndex).getStringCellValue();
		        		        			if (!secondRow.getCell(colIndex).getStringCellValue().equals("")) {
			        		        			List<Integer> list = Arrays.asList(tissueAccIndex, mrnIndex, protocolAccIndex, 0);
			        		        			if (Collections.max(list) > 0) {
			        		        				markerMap.put(cellStr, colIndex);
			        		        			}
		        		        			}
		        		        		}
		        		        	}
		        		        	colIndex ++;		        		    	
		        	            }
		        		        
		        		        // parse second row in each read block and retrieve attributes and their column positions.
		        		        colIndex = 0;
		        		        for (Cell ce : secondRow)
		        	            {
		        		        	if(ce != null) {
		        		        		if (!ce.getStringCellValue().equals("")) {
		        		        			String cellStr = ce.getStringCellValue();
			        		        		attributeMap.put(colIndex, new String(attributeType(cellStr)));	
		        		        		}
		        		        	}
		        		        	colIndex ++;
		        	            }
		        		        // go to the next iteration
		        				continue;
		        			}
		        		}
        			}
        			
		        	
	        		// read block begins
	        		if (readFlag == 1) {
	        			
	        			// read block stops at empty row
	        			if (row == null) {
	        				readFlag = 0;
	        			} 
	        			else {
	        				Cell cell = row.getCell(0);
	        				// read block stops if the first cell is null
	        				if (cell == null) {
	        					readFlag = 0;
	        				}
	        				else {
		        				int celltype = cell.getCellType();
		        				//read block stops if the first cell is "Average"
			        			if (celltype == Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase("Average")) {
			        				readFlag = 0;
			        			}
			        			else {
			        				// read data rows and write out to text file		        				
			        				if (mrnIndex != -1 ) {
			        					cell = row.getCell(mrnIndex);
			        					if (cell != null) {
			        						// force cell type to string
			        						cell.setCellType(Cell.CELL_TYPE_STRING);
			        						mrn = cell.getStringCellValue();

			        					}
			        				}
			        				if (tissueAccIndex != -1) {
			        					cell = row.getCell(tissueAccIndex);
			        					if (cell != null) {
			        						// force cell type to string
			        						cell.setCellType(Cell.CELL_TYPE_STRING);
			        						accession = cell.getStringCellValue();
			        					}
			        					
			        				}
			        				
			        				// iterate markermap, get column range for each biomarker		        				
			        				Iterator<String> entryItr = markerMap.keySet().iterator();
			        				String marker = entryItr.next();
			        				int start = markerMap.get(marker);
			        				int end = 0;
			        				double[] dataArray = new double[4];
			        				while (entryItr.hasNext()) {
			        					String nextMarker = entryItr.next();
			        					end = markerMap.get(nextMarker);
			        					dataArray =	getCellValue(row, start, end, attributeMap);
			        					writer.println(protocol + "|" + mrn + "|" + accession + "|" + marker + "|" + type + "|" + 
			        								dataArray[0] + "|" + dataArray[1] + "|" + dataArray[2] + "|" + dataArray[3]);
			        					dataArray = new double[4];
			        					marker = nextMarker;
			        					start = markerMap.get(marker);
			        				}
			        				// handle the last marker
			        				List<Integer> keys = new ArrayList<Integer>(attributeMap.keySet());
			        				end = keys.get(keys.size() - 1) + 1;
			        				dataArray =	getCellValue(row, start, end, attributeMap);
			        				writer.println(protocol + "|" + mrn + "|" + accession + "|" + marker + "|" + type + "|" + 
		    								dataArray[0] + "|" + dataArray[1] + "|" + dataArray[2] + "|" + dataArray[3]);
			        			}
	        				}
	        			
		        			
	        			}
	        		}
	        	
	        	}
	        
	        }
	        writer.close();
	        if (deleteFlag == 0) {
	        	System.out.println("Nothing read from " + in.getAbsolutePath());
	        	out.delete();
	        	return 0;
	        }
	        else{
	        	System.out.println("File conversion complete. " + out.getName());
	        	return 1;
	        }
	        
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
		} catch (IOException e) {
	        e.printStackTrace();
		}
		return 1;
	}

	/** get cell value from excel row for corresponding attributes
	 * 
	 * @param row -- current row in spreadsheet
	 * @param start -- starting column index for each marker
	 * @param end -- ending column index for each marker
	 * @param attributeMap -- linkedhashmap for attributes: <col_index, attribute>
	 * @return double array of 4 (IM, CT, N, TZ)
	 */
	private static double[] getCellValue(Row row, int start,
			int end, Map<Integer, String> attributeMap) {
		double[] array = new double[4];
		if (start < end) {
			for (int i=start; i < end; i++) {
				Cell cell;
				if (attributeMap.get(i).equalsIgnoreCase("IM")) {
					cell = row.getCell(i);
					if (cell != null) {
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							array[0] = cell.getNumericCellValue();
						}
					}
				}
				if (attributeMap.get(i).equalsIgnoreCase("CT")) {
					cell = row.getCell(i);
					if (cell != null) {
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							array[1] = cell.getNumericCellValue();
						}
					}
				}
				if (attributeMap.get(i).equalsIgnoreCase("N")) {
					cell = row.getCell(i);
					if (cell != null) {
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							array[2] = cell.getNumericCellValue();
						}
					}
				}
				if (attributeMap.get(i).equalsIgnoreCase("TZ")) {
					cell = row.getCell(i);
					if (cell != null) {
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							array[3] = cell.getNumericCellValue();
						}
					}
				}
				
			}
			
		}
		return array;
		
	}

	private static String attributeType(String cellStr) {
		String str = cellStr.toLowerCase();
		if (str.endsWith("im")) 
			return "IM";
		else if (str.endsWith("ct"))
			return "CT";
		else if (str.endsWith("n"))
			return "N";
		else if (str.endsWith("tz"))
			return "TZ";
		else
			return "CT";
	}
}