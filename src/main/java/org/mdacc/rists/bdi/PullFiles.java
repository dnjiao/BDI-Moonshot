package org.mdacc.rists.bdi;

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
import org.mdacc.rists.bdi.db.utils.DBConnection;
import org.mdacc.rists.bdi.db.utils.FileLocationUtil;
import org.mdacc.rists.bdi.db.utils.FileQueueUtil;
import org.mdacc.rists.bdi.db.utils.FileTransferAuditUtil;
import org.mdacc.rists.bdi.utils.XMLParser;

public class PullFiles {
	
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	// Counter for file copied
	static int fileCounter = 0;
	// Counter for file converted successfully
	static int convertCounter = 0;
	static List<String> dirs = new ArrayList<String>();
	static List <String> TYPES = Arrays.asList("vcf", "cnv", "exon", "gene", "junction", "mapping", "flowcyto", "immunopath", "fm-val", "fm-xml");
	final static Connection CONN = DBConnection.getConnection();
	final static String DESTROOT = "/rsrch1/rists/moonshot/data";
	final static String ENV = System.getenv("DEV_ENV");
	
	public static void main(String[] args) throws Exception {

		if (args.length != 1 && args.length != 2) {
			System.err.println("Usage: PullFiles [type] (optional: [xml_path])");
			System.exit(1);
		}
		
		String type = args[0].toLowerCase();
		if (!TYPES.contains(type)) {
			System.err.println("Invalid type.");
			System.exit(1);
		}
		
		String dest = DESTROOT + "/" + ENV + "/" + type;
		// no need for conf xml with sources for the following 3 types
		if (type.equalsIgnoreCase("mapping")) {
			PullMappingFiles(dest);
		}
		else if (type.equalsIgnoreCase("fm-val")) {
			dest = DESTROOT + "/" + ENV + "/foundation/validation";
			PullFMValFiles(dest);
		}
		else if (type.equalsIgnoreCase("fm-xml")) {
			dest = DESTROOT + "/" + ENV + "/foundation/xml";
			PullFMXmlFiles(dest);
		}
		else {
			if (args.length != 2) {
				System.err.println("Source xml file is missing.");
				System.exit(1);
			}
			if (!new File(args[1]).exists()) {
				System.err.println("File " + args[1] + " does not exist.");
				System.exit(1);
			}
			List<String> sourceList = XMLParser.readSourceXML(args[0], args[1]);
			if (sourceList == null) {
				System.err.println("No sources for " + args[0] + " in " + args[1]);
				System.exit(1);
			} else {
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
		}
		System.out.println(Integer.toString(fileCounter) + " " + type + " files transferred.");
		System.out.println(Integer.toString(convertCounter) + " files preprocessed.");
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
			if (WorkflowUtils.isMapping(file)) {
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
		
		// if no new files found, return 0
		if (fileList.size() == 0) {
			System.out.println("No new files found since last transfer.");
			return 0;
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
		
		try {
			// get path of last mapping file transferred
			String latestFilePath = FileTransferAuditUtil.getLatestFile(CONN, "mapping");
			Boolean latestBool = false;
			if (latestFilePath != null) {
				System.out.println("The last file pulled: " + latestFilePath);
				System.out.println(fileList.get(0).getAbsolutePath());
				File latestFile = new File(latestFilePath);
				latestBool = FileUtils.contentEquals(fileList.get(0), latestFile);
			}
			
			// keep distinct ones
			List<File> uniqueFiles = new ArrayList<File>();
			DateTime ts = null;
			for (int i = 0; i < fileList.size(); i++) {		
				File file = fileList.get(i);
				// compare content of first file with last file from last batch; compare adjacent two files for the current batch
				if (i == 0 && !latestBool || i > 0 && !FileUtils.contentEquals(file, fileList.get(i - 1))) {
					ts = new DateTime();
					List<String> auditFileList = new ArrayList<String>();
					String newName = file.getName().split("\\.")[0] + "_" + FORMAT.print(current) + ".txt";;
					File newFile = new File(dest, newName);
					WorkflowUtils.fixMappingFile(file, newFile);
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

	/**
	 * Download foundation medicine files from ftp server, validate.
	 * @param dest - data directory for foundation medicine in RIStore
	 */
	private static void PullFMXmlFiles(String dest) {
		// call bash script to transfer mapping files by sftp
		try {
			File archive = new File(dest, "archive");
			if (!archive.exists()) {
				System.out.println("Creating folder " + archive.getAbsolutePath());
				archive.mkdir();
			}
			String[] cmd = new String[]{"/bin/bash", "/rsrch1/rists/moonshot/apps/sh/sftp-found-xml.sh", dest + "/archive"};
//			String[] cmd = new String[]{"/bin/bash", "-c", "rsync -auv /rsrch1/rists/moonshot/data/foundation/FoundationMedicine/test/*.xml " + archive.getAbsolutePath()};
			String source = "ftp.mdanderson.org";
			System.out.println(cmd);
			
			Process p = Runtime.getRuntime().exec(cmd);
			String line;
			
			// stdout and stderr of bash script
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = err.readLine()) != null) {
				System.err.println(line);
			}
			p.waitFor();
			in.close();
			err.close();
			// process files and keep only the new ones since last pull
			DateTime lastTS = FileLocationUtil.getLastTimeStamp(CONN, "fm-xml", source);
			System.out.println("last_ts for foundation xml: " + lastTS);
			
			File[] files = archive.listFiles();
			List<File> fileList = new ArrayList<File>();
			// delete older files
			for (File file : files) {
				if (WorkflowUtils.isType(file.getName(), "fm-xml")) {
					if (lastTS == null || (lastTS != null && lastTS.isBefore(file.lastModified()))) {
						fileList.add(file);
					}
					else {
						file.delete();
					}
				}
				else {
					file.delete();
				}
			}
			
			DateTime current = new DateTime();
			// process foundation files
			for (File file : fileList) {
				
				List<String> auditFileList = new ArrayList<String>();
				String newXMLName = file.getName().split("\\.")[0] + "_" + FORMAT.print(current) + ".xml";
				File newXMLFile = new File(dest, newXMLName);
				//copy file to out of archive folder
				Files.copy(file.toPath(), newXMLFile.toPath());
				FileTransferAuditUtil.insertRecord(CONN, source + "/" + file.getName(), newXMLFile.getAbsolutePath(), "sftp");
				
				//validation
//				String md5 = GenChecksum.getMd5(newXMLFile);
//				System.out.println("Validating " + newXMLFile.getAbsolutePath() + " with checksum " + md5);
//				int isValidated = FileChecksumUtil.ValidateChecksum(newXMLFile.getAbsolutePath(), md5, "FM");
//				System.out.println("validation return " + Integer.toString(isValidated));
				
				int fileQueueId = FileQueueUtil.insertRecord(CONN, newXMLFile.getAbsolutePath(), "fm-xml");
				fileCounter ++;
				auditFileList.add(newXMLFile.getAbsolutePath());
				FileTransferAuditUtil.updateFileQueueId(CONN, auditFileList, fileQueueId);
				FileLocationUtil.setLastTimeStamp(CONN, "fm-xml", source, current);
				// delete file from archive dir
				file.delete();
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	/**
	 *  Pull validation files for Foundation from ftp server
	 * @param dest - data directory for foundation medicine in RIStore
	 */
	private static void PullFMValFiles(String dest) {
		try {
			int counter = 0;
			// create directory 'archive' under foundation-validation dir if not existed
			File archive = new File(dest, "archive");
			if (!archive.exists()) {
				System.out.println("Creating folder " + archive.getAbsolutePath());
				archive.mkdir();
			}
			
			// run bash script to download validation files from ftp server
//			String[] cmd = new String[]{"/bin/bash", "/rsrch1/rists/moonshot/apps/sh/sftp-found-val.sh", dest};
			String[] cmd = new String[]{"/bin/bash", "-c", "rsync -auv /rsrch1/rists/moonshot/data/foundation/FoundationMedicine/*.csv " + archive.getAbsolutePath()};
			System.out.println(cmd);

			// capture stdout and stderr from running bash script
			Process p = Runtime.getRuntime().exec(cmd);
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = err.readLine()) != null) {
				System.err.println(line);
			}
			p.waitFor();
			in.close();
			err.close();
			
			// process files and keep only the new ones since last pull
			String source = "ftp.mdanderson.org";
			DateTime lastTS = FileLocationUtil.getLastTimeStamp(CONN, "fm-val", source);
			System.out.println("last_ts for foundation validation: " + lastTS);
			
			File[] files = archive.listFiles();
			List<File> fileList = new ArrayList<File>();
			// delete older files
			for (File file : files) {
				if (WorkflowUtils.isType(file.getName(), "fm-val")) {
					if (lastTS == null || (lastTS != null && lastTS.isBefore(file.lastModified()))) {
						fileList.add(file);
					}
					else {
						file.delete();
					}
				}
				else {
					file.delete();
				}	
			}
			
			// process validation files
			DateTime current = new DateTime();
			for (File file : fileList) {
				List<String> auditFileList = new ArrayList<String>();
				String newName = file.getName().split("\\.")[0] + "_" + FORMAT.print(current) + ".csv";
				File newValFile = new File(dest, newName);
				Files.copy(file.toPath(), newValFile.toPath());
				System.out.println(file.getAbsolutePath() + " copied to " + newValFile.getAbsolutePath());
				FileTransferAuditUtil.insertRecord(CONN, source + "/" + file.getName(), newValFile.getAbsolutePath(), "sftp");
				
				int fileQueueId = FileQueueUtil.insertRecord(CONN, newValFile.getAbsolutePath(), "fm-val");
				counter ++;
				auditFileList.add(newValFile.getAbsolutePath());
				FileTransferAuditUtil.updateFileQueueId(CONN, auditFileList, fileQueueId);
				FileLocationUtil.setLastTimeStamp(CONN, "fm-val", source, current);
				file.delete();
			}
			System.out.println(Integer.toString(counter) + " foundation validation files transferred.");
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				   if(!file.isHidden()) {  // exclude hidden files
					   if (WorkflowUtils.isType(fileName, TYPE)) {	
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
								   System.out.println("Changing permission for " + toPath.toString());
								   Runtime.getRuntime().exec(new String[]{"chmod", "664", toPath.toString()});			
								  
								   addDirs(srcPath);
								   FileTransferAuditUtil.insertRecord(CONN, fromPath.toString(), toPath.toString(), "cp");
								   int imtSuccess = 1;
								   if (TYPE.equals("immunopath")) {
									   newName = WorkflowUtils.switchExt(newName, "psv");
									   toPath = Paths.get(DEST, newName);
									   imtSuccess = WorkflowUtils.immunoPsv(oldPath.toFile(), toPath.toFile());
								   }
								   if (TYPE.equals("flowcyto")) {
									   newName = WorkflowUtils.switchExt(newName, "psv");
									   toPath = Paths.get(DEST, newName);
									   imtSuccess = WorkflowUtils.flowPsv(oldPath.toFile(), toPath.toFile());
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