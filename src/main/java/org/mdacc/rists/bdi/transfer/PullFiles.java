package org.mdacc.rists.bdi.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.rists.bdi.dbops.FileLocationUtil;
import org.mdacc.rists.bdi.dbops.FileQueueUtil;
import org.mdacc.rists.bdi.dbops.FileTransferAuditUtil;
import org.mdacc.rists.bdi.dbops.DBConnection;
import org.mdacc.rists.bdi.xml.XmlParser;

public class PullFiles {
	
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	// Counter for file copied
	static int fileCounter = 0;
	// Counter for file converted successfully
	static int convertCounter = 0;
	static List<String> dirs = new ArrayList<String>();
	static List <String> TYPES = Arrays.asList("vcf", "cnv", "exon", "gene", "junction", "mapping", "flowcyto", "immunopath");
	final static Connection CONN = DBConnection.getConnection();
//	final static String DESTROOT = "/Users/djiao/Work/moonshot/data";
	final static String DESTROOT = "/rsrch1/rists/moonshot/data";
	final static String ENV = System.getenv("DEV_ENV");
	
	public static void main(String[] args) {

		if (args.length != 2) {
			System.err.println("Usage: PullFiles [xml_path] [type]");
			System.exit(1);
		}
		if (!new File(args[0]).exists()) {
			System.err.println("File " + args[0] + " does not exist.");
			System.exit(1);
		}
		String type = args[1].toLowerCase();
		if (!TYPES.contains(type)) {
			System.err.println("Invalid type.");
			System.exit(1);
		}
		
		List<String> sourceList = XmlParser.readXML(args[0], args[1]);
		if (sourceList == null) {
			System.err.println("No sources for " + args[1] + " in " + args[0]);
			System.exit(1);
		} else {
			String dest = DESTROOT + "/" + ENV + "/" + type;
			if (type.equalsIgnoreCase("mapping")) {
				PullMappingFiles(dest);
			}
			else {
				DateTime current = new DateTime();
			
				for (String src : sourceList) {
					if (!new File(src).isDirectory()) {
						System.err.println("Source Dir " + src + " is not a directory.");
					}
					else {
						walkFiles(src, dest, type, current);
						for (String d : dirs) {
							FileLocationUtil.setLastTimeStamp(CONN, type, d, current);
						}
					}
				}
			}
			System.out.println(Integer.toString(fileCounter) + " " + type + " files transferred.");
			System.out.println(Integer.toString(convertCounter) + " files preprocessed.");
		}
	}
	
	
	private static void PullMappingFiles(String dest) {
		// call bash script to transfer mapping files by sftp
		try {
			File archive = new File(dest, "archive");
			if (!archive.exists()) {
				System.out.println("Creating folder " + archive.getAbsolutePath());
				archive.mkdir();
			}
			String[] cmd = new String[]{"/bin/bash", "/rsrch1/rists/moonshot/apps/sh/sftp.sh", dest + "/archive"};
			String source = "prodinformat";
			
			Process p = Runtime.getRuntime().exec(cmd);
			String line;
			
			// stdout and stderr of bash script
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = err.readLine()) != null) {
				System.out.println(line);
			}
			p.waitFor();
			fileCounter = iterateMappingFiles(source, dest, new DateTime());
			in.close();
			err.close();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	/**
	 * process mapping files from last pull, deleting, logging and auditing
	 * @param dest - dir of mapping files
	 * @param insertWriter - writer of insert log
	 * @param logWriter - writer of pull log
	 * 
	 */
	private static int iterateMappingFiles(String source, String dest, DateTime current) {
		DateTime lastTS = FileLocationUtil.getLastTimeStamp(CONN, "mapping", source);
		System.out.print("last_ts for mapping: ");
		System.out.println(lastTS);
		File destDir = new File(dest + "/archive");
		int fileCounter = 0;
		File[] files = destDir.listFiles();
		List<File> fileList = new ArrayList<File>();
		// delete older files and non mapping files
		for (File file : files) {
			if (TransferUtils.isMapping(file)) {
				if (lastTS == null || (lastTS != null && lastTS.isBefore(file.lastModified()))) {
					String lastFilePath = FileQueueUtil.getLatestFile(CONN, dest);
					if (lastFilePath != null) {
						File lastFile = new File(lastFilePath);
						try {
							if (!FileUtils.contentEquals(file, lastFile)) {
								fileList.add(file);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else {
						fileList.add(file);
					}
				}
				else {
					file.delete();
					System.out.println(file.getAbsolutePath() + " is old.");
				}
			}
			else {
				file.delete();
				System.out.println(file.getAbsolutePath() + " is not a valid mapping file.");
			}
				
		}
		
		// sort files based on original timestamp
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				String fileName1 = f1.getName();
				String fileName2 = f2.getName();
				int year1 = Integer.parseInt(fileName1.substring(12, 16));
				int year2 = Integer.parseInt(fileName2.substring(12, 16));
				int month1 = Integer.parseInt(fileName1.substring(16, 18));
				int month2 = Integer.parseInt(fileName2.substring(16, 18));
				int day1 = Integer.parseInt(fileName1.substring(18, 20));
				int day2 = Integer.parseInt(fileName2.substring(18, 20));
				if (year1 != year2) {
					return year1 - year2;
				}
				else {
					if (month1 != month2) {
						return month1 - month2;
					}
					else {
							return day1 - day2;
					}
				}
			}

		});
		
		// keep distinct ones
		List<File> uniqueFiles = new ArrayList<File>();
		try {
			for (int i = 0; i < fileList.size(); i++) {		
				File file = fileList.get(i);
				if (i == 0 || (i > 0 && !FileUtils.contentEquals(file, fileList.get(i - 1)))) {
					DateTime ts = new DateTime();
					List<String> auditFileList = new ArrayList<String>();
					String newName = file.getName().split("\\.")[0] + "_" + FORMAT.print(current) + ".txt";;
					File newFile = new File(dest, newName);
					TransferUtils.fixMappingFile(file, newFile);
					FileTransferAuditUtil.insertRecord(CONN, source + "/" + file.getName(), newFile.getAbsolutePath(), "sftp");
					int fileQueueId = FileQueueUtil.insertRecord(CONN, newFile.getAbsolutePath(), "mapping");
					fileCounter ++;
					auditFileList.add(newFile.getAbsolutePath());
					FileTransferAuditUtil.updateFileQueueId(CONN, auditFileList, fileQueueId);
					FileLocationUtil.setLastTimeStamp(CONN, "mapping", source, ts);
					uniqueFiles.add(newFile);
					System.out.println(newFile.getAbsolutePath());
				}
			}
			
			//delete files in archive directory
			for (File f : fileList) {
				f.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileCounter;
	}
	
	public static void fileCounterMethod(){
		fileCounter++;
	}
	
	public static void convertCounterMethod(){
		convertCounter++;
	}
	
	private static void addDirs(String dirPath) {
		if (! dirs.contains(dirPath)) 
			dirs.add(dirPath);
	}


	public static void walkFiles(String source, String dest, String type, DateTime current) {		
    	Path top = Paths.get(source);
    	final String TYPE = type;    	
    	final String DEST = dest;  
    	final DateTime CURRENT = current;
    	
    	try {
			Files.walkFileTree(top, new SimpleFileVisitor<Path>()
			{  
			   @Override
			   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
			   {
				   File file = filePath.toFile();
				   String fileName = filePath.getFileName().toString();		
				   
				   if (TransferUtils.isType(fileName, TYPE)) {	
//					   System.out.println(fileName + " " + TYPE);
					   String srcPath = file.getParent();
					   DateTime lastDt = FileLocationUtil.getLastTimeStamp(CONN, TYPE, srcPath);
					   if (lastDt == null || (lastDt != null && lastDt.isBefore(file.lastModified()))) {
						   List<String> files = new ArrayList<String>();
						   String newName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + FORMAT.print(CURRENT) + fileName.substring(fileName.lastIndexOf("."));
						   newName=newName.replaceAll("'", "");
						   Path fromPath = filePath;
						   Path toPath = Paths.get(DEST, newName);
						   Path oldPath = toPath;						   
						   Process p = Runtime.getRuntime().exec(new String[]{"cp", fromPath.toString(), toPath.toString()});
						   try {
							   p.waitFor();
						   } catch (InterruptedException e) {
								e.printStackTrace();
						   }
						   // print out output and error running the commmand
						   BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						   BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
						   String outStr = null;
						   while ((outStr = stdInput.readLine()) != null) {
							   System.out.println(outStr);
						   }
						   String errStr = null;
						   while ((errStr = stdError.readLine()) != null) {
							   System.out.println(errStr);
						   }
						   if (toPath.toFile().exists()) {							   
							   System.out.println("Transfer completed from " + fromPath.toString() + " to " + toPath.toString());
							   fileCounterMethod();
							   String cmd = "chmod 664 " + toPath.toString();
							   Runtime.getRuntime().exec(cmd);
							  
							   addDirs(srcPath);
							   FileTransferAuditUtil.insertRecord(CONN, fromPath.toString(), toPath.toString(), "cp");
							   int imtSuccess = 1;
							   if (TYPE.equals("immunopath")) {
								   newName = TransferUtils.switchExt(newName, "psv");
								   toPath = Paths.get(DEST, newName);
								   imtSuccess = TransferUtils.immunoPsv(oldPath.toFile(), toPath.toFile());
							   }
							   if (TYPE.equals("flowcyto")) {
								   newName = TransferUtils.switchExt(newName, "psv");
								   toPath = Paths.get(DEST, newName);
								   imtSuccess = TransferUtils.flowPsv(oldPath.toFile(), toPath.toFile());
							   }
							   
							   if (imtSuccess == 1) {
								   int fileQueueId = FileQueueUtil.insertRecord(CONN, toPath.toString(), TYPE);
								   convertCounterMethod();
								   files.add(oldPath.toString());
								   FileTransferAuditUtil.updateFileQueueId(CONN, files, fileQueueId);
							   }
						   } else {
							   System.out.println("Transfer not successful: " + fromPath.toString());
						   }					   
					   }
				   }
			   	
				   return FileVisitResult.CONTINUE;
			   }
			});
			
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	} 
    	
	}
}