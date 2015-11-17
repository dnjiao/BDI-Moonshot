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
			String[] cmd = new String[]{"/bin/bash", "/rsrch1/rists/moonshot/apps/sh/sftp.sh", dest};
			String source = "Informat";
			
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
		File destDir = new File(dest);
		int fileCounter = 0;
		List<String> files;
		
		for (File file : destDir.listFiles()) {
			if (file.isDirectory() == false) { // ignore directories
				System.out.println(file.getName());
				if (TransferUtils.isMapping(file)) {
					System.out.println(file.getName() + " last modified ts: " + FORMAT.print(file.lastModified()));
					if (lastTS == null || (lastTS != null && lastTS.isBefore(file.lastModified()))) {
						files = new ArrayList<String>();
						String fileName = file.getName();
						String newName = fileName.split("\\.")[0] + "_" + FORMAT.print(current) + "." + fileName.split("\\.")[1];
						File newfile = new File(file.getParent(), newName);
						TransferUtils.removeReturnChar(file, newfile);
						System.out.println(newfile.getName());
						FileTransferAuditUtil.insertRecord(CONN, source + "/" + file.getName(), newfile.getAbsolutePath(), "sftp");
						file.delete();
						int fileQueueId = FileQueueUtil.insertRecord(CONN, newfile.getAbsolutePath(), "mapping");
						fileCounter ++;
						files.add(newfile.getAbsolutePath());
						FileTransferAuditUtil.updateFileQueueId(CONN, files, fileQueueId);
						DateTime ts = new DateTime();
						FileLocationUtil.setLastTimeStamp(CONN, "mapping", source, ts);
					}
				}
				else {
					file.delete();
					System.out.println(file.getName() + " is not a mapping file. Deleted.");
				}
			}
			else {
				file.delete();
			}
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
					   System.out.println(fileName + " " + TYPE);
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