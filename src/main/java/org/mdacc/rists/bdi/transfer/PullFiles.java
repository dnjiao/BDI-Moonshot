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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.rists.bdi.dbops.FileLocationUtil;
import org.mdacc.rists.bdi.dbops.FileQueueUtil;
import org.mdacc.rists.bdi.dbops.FileTransferAuditUtil;
import org.mdacc.rists.bdi.dbops.DBConnection;
import org.mdacc.rists.bdi.xml.WorkFlow;
import org.mdacc.rists.bdi.xml.XmlParser;

public class PullFiles {
	
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	static int fileCounter = 0;
	static List<String> dirs = new ArrayList<String>();
	final static Connection CONN = DBConnection.getConnection();
//	final static String DESTROOT = "/rsrch1/rists/moonshot/data";
	final static String DESTROOT = "/Users/djiao/Work/moonshot/data";
	final static String ENV = System.getenv("DEV_ENV");
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: PullFiles [xml_file]");
			System.exit(1);
		}
		if (!new File(args[0]).exists()) {
			System.err.println("File " + args[0] + " does not exist.");
			System.exit(1);
		}
		List<WorkFlow> flowList = XmlParser.readXML(args[0]);
		if (flowList == null) {
			System.err.println("No flow definition found in " + args[0]);
			System.exit(1);
		} else {
			for (WorkFlow flow : flowList) {
				String dest = DESTROOT + "/" + ENV + "/" + flow.getType();
				String type = flow.getType();
				if (flow.getType().equalsIgnoreCase("mapping")) {
					PullMappingFiles(dest);
				}
				else {
					DateTime current = new DateTime();
					
					for (String src : flow.getSources()) {
						if (!new File(src).isDirectory()) {
							System.err.println("Source Dir " + src + " is not a directory.");
						}
						else {
							walkFiles(src, dest, type, current);
							for (String d : dirs) {
								System.out.println("Dir: " + d);
								FileLocationUtil.setLastTimeStamp(CONN, type, d, current);
							}
						}
					}
				}
				System.out.println("Total " + Integer.toString(fileCounter) + " " + type + " files pulled successfully.");
			}
		}
	}
	
	
	private static void PullMappingFiles(String dest) {
		// call bash script to transfer mapping files by sftp
		try {
			if (!new File(dest, "archive").exists()) {
				System.out.println("/archive  does not exist in " + dest);
				System.exit(1);;
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
					fileList.add(file);
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
		
		for (int i = 0; i < fileList.size() - 1; i++) {
			File first = fileList.get(i);
			File second = fileList.get(i+1);
			if (!TransferUtils.isSameFile(first, second)) {
				List<String> auditFileList = new ArrayList<String>();
				String newName = first.getName().split("\\.")[0] + "_" + FORMAT.print(current) + ".txt";
				File newFile = new File(dest, newName);
				TransferUtils.removeReturnChar(first, newFile);
				System.out.println(newName);
				FileTransferAuditUtil.insertRecord(CONN, source + "/" + first.getName(), newFile.getAbsolutePath(), "sftp");
				int fileQueueId = FileQueueUtil.insertRecord(CONN, newFile.getAbsolutePath(), "mapping");
				fileCounter ++;
				auditFileList.add(newFile.getAbsolutePath());
				FileTransferAuditUtil.updateFileQueueId(CONN, auditFileList, fileQueueId);
				DateTime ts = new DateTime();
				FileLocationUtil.setLastTimeStamp(CONN, "mapping", source, ts);
			}
			first.delete();
		}
		
		return fileCounter;
	}
	
	public static void counterMethod(){
		fileCounter++;
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
						   String newName = fileName.substring(0, fileName.lastIndexOf(".")) + "_" + FORMAT.print(CURRENT) + fileName.substring(fileName.lastIndexOf("."));
						   Path fromPath = filePath;
						   Path toPath = Paths.get(DEST, newName);
						   Path oldPath = toPath;
						   String cmd = "cp " + fromPath.toString() + " " + toPath.toString();				  
						   List<String> files = new ArrayList<String>();
						   Runtime.getRuntime().exec(cmd);
						   addDirs(srcPath);
						   FileTransferAuditUtil.insertRecord(CONN, fromPath.toString(), toPath.toString(), "cp");
						   int imtSuccess = 1;
						   if (TYPE.equals("immunopath")) {
							   newName = TransferUtils.switchExt(newName, "tsv");
							   toPath = Paths.get(DEST, newName);
							   PreProcessing.immunoTsv(oldPath.toFile(), toPath.toFile());
						   }
						   if (TYPE.equals("flowcyto")) {
							   newName = TransferUtils.switchExt(newName, "tsv");
							   toPath = Paths.get(DEST, newName);
							   imtSuccess = PreProcessing.flowTsv(file, toPath.toFile());
						   }
						   if (imtSuccess == 1) {
							   int fileQueueId = FileQueueUtil.insertRecord(CONN, toPath.toString(), TYPE);
							   counterMethod();
							   files.add(toPath.toString());
							   FileTransferAuditUtil.updateFileQueueId(CONN, files, fileQueueId);
						   }
							   
					   }
				   }
			      return FileVisitResult.CONTINUE;
			   }
			});
			
    	} catch (IOException e) {
    		e.printStackTrace();
    	} 
    	
	}
}