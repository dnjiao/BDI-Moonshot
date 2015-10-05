package org.mdacc.rists.bdi.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.rists.bdi.datafiles.FileConversion;
import org.mdacc.rists.bdi.datafiles.FlowData;
import org.mdacc.rists.bdi.dbops.FileLocationUtil;
import org.mdacc.rists.bdi.dbops.FileTransferAuditUtil;
import org.mdacc.rists.bdi.dbops.DBConnection;
import org.mdacc.rists.bdi.hibernate.FileLocation;
import org.mdacc.rists.bdi.hibernate.FileType;
import org.mdacc.rists.bdi.hibernate.HibernateUtil;


public class PullFiles {
	
//	final static String DEST_ROOT = "/Users/djiao/Work/moonshot/dest";
	final static String DEST_ROOT = "/rsrch1/rists/moonshot/data/dev";
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	static int fileCounter = 0;
	final static Connection CONN = DBConnection.getConnection();
	
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
	    if (TYPE == null) {
	    	System.out.println("ERROR: Environment variable TYPE not set correctly.");
	    	System.exit(1);
	    }
	    executeTransfer(TYPE);
	}
	
	
	private static void executeTransfer(String type) {
	    
	    final String DEST = DEST_ROOT + "/" + type;
	    String source;
	    File destDir = new File(DEST);
	    if (!destDir.exists()){
	        System.err.println("ERROR: Destination path " + DEST + " does not exist.");
	        System.exit(1);
	    }
	    String update;
	    try {
		    // get the string for current time
		    DateTime current = new DateTime();
		    String dtStr = FORMAT.print(current);
		    
		    Map<String, String> env = System.getenv();
		    for (String envName : env.keySet()) {
		    	if (envName.contains("SOURCE_DIR")) {
		    		source = env.get(envName);
		    		if (source.length() > 3) {
		    			if (type.equals("mapping")) {
		    				// call bash script to transfer mapping files by sftp
		    				try {
		    					String[] cmd = new String[]{"/bin/bash", "/rsrch1/rists/moonshot/apps/sh/sftp.sh", DEST_ROOT + "/mapping"};
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
									System.out.println(line);
								}
								p.waitFor();
								in.close();
								err.close();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
		    			    fileCounter = processMappingFiles(source, DEST, current);
		    			}
		    			else if (type.equals("flowcyto")) {
		    				processFlowFiles(source, DEST);
		    				FileLocationUtil.setLastTimestamp(CONN, "mapping", "Informat server", current);
		    			}
		    			else {
		    				
		    				if (new File(source).isDirectory()) {
		    					DateTime lastTS = FileLocationUtil.getLastTimeStamp(CONN, type, source);
		    					cpFiles(source, DEST, type, current, lastTS);
		    					FileLocationUtil.setLastTimestamp(CONN, "mapping", "Informat server", current);
		    				}
				    			
				    		else
				    			System.err.println("Source Dir " + envName + "(" + source + ")" + " is not a directory.");
		    			}
		    		}	
		    	}
		    }
		    
		    System.out.println("Total " + Integer.toString(fileCounter) + " " + type + " files pulled successfully.");
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
	private static int processMappingFiles(String source, String dest, DateTime current) {
		DateTime lastTS = FileLocationUtil.getLastTimeStamp(CONN, "mapping", "Informat server");
		File destDir = new File(dest);
		int fileCounter = 0;
		
		for (File file : destDir.listFiles()) {
			if (file.isDirectory() == false) { // ignore directories
				if (TransferUtils.isMapping(file)) {
					if (lastTS.isBefore(file.lastModified())) {
						String fileName = file.getName();
						String newName = fileName.split("\\.")[0] + "_" + FORMAT.print(current) + "." + fileName.split("\\.")[1];
						File newFile = new File(file.getParent(), newName);
						file.renameTo(newFile);
						int fileQueueId = FileTransferAuditUtil.insertRecord(CONN, source + "/" + file.getName(), file.getAbsolutePath(), "sftp");
						if (fileQueueId == 0) {
							file.delete();
							System.err.println("Cannot insert " + file.getAbsolutePath() + " to database.");
						}
						else {
							
							fileCounter ++;
						}
					}
				}
				else {
					file.delete();
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
	
	
	private static void processFlowFiles(String source, String dest) {
		Path top = Paths.get(source);
    	final String DEST = dest;    	
    	try {
    		Files.walkFileTree(top, new SimpleFileVisitor<Path>()
			{  
			   @Override
			   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
			   {
				   File dir = filePath.toFile();
				   if (dir.isDirectory()) {
					   String dirName = dir.getName();
					   if (dirName.toLowerCase().endsWith("moonshot")) {
						   DateTime now = new DateTime();
						   String newName = dirName.split(" ")[0] + "_" + FORMAT.print(now) + ".tsv";
						   String outFile = DEST + "/" + dirName.split(" ")[0] + "_" + FORMAT.print(now) + ".tsv";
						   FlowData.BdiFlowSummary(dir, outFile); 
						   counterMethod();
						   if (FileTransferAuditUtil.insertRecord(CONN, dir.toString(), outFile, "Process") == 0) {
							   System.out.println("not inserted " + dir.toString() + "\t" + outFile);
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


	public static void cpFiles(String source, String dest, String type, DateTime current, DateTime lastTS) {		
    	Path top = Paths.get(source);
    	final String TYPE = type;    	
    	final String DEST = dest;  
    	final DateTime CURRENT = current;
    	final DateTime LAST = lastTS;
    	
    	try {
			Files.walkFileTree(top, new SimpleFileVisitor<Path>()
			{  
			   @Override
			   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
			   {
				   String fileName = filePath.getFileName().toString();				   
				   if (TransferUtils.isType(fileName, TYPE)) {	
					   String newName = fileName.split("\\.")[0] + "_" + FORMAT.print(CURRENT) + "." + fileName.split("\\.")[1];
					   String srcPath = filePath.getParent().toString();
					   
					   Path fromPath = filePath;
					   Path toPath = Paths.get(DEST, newName);
					   Path oldPath = toPath;
					   String cmd = "cp " + fromPath.toString() + " " + toPath.toString();
					   
					   if (LAST.isBefore(fromPath.toFile().lastModified())) {  // add only new files 
						   Runtime.getRuntime().exec(cmd);
						   if (TYPE.equals("immunopath")) {
							   newName = TransferUtils.switchExt(newName, "tsv");
							   toPath = Paths.get(DEST, newName);
							   FileConversion.immunoTsv(oldPath.toFile(), toPath.toFile());
						   }
						   if (FileTransferAuditUtil.insertRecord(CONN, fromPath.toString(), toPath.toString(), "cp") == 0) {
							   toPath.toFile().delete();
						   }
						   else 
							   counterMethod();  
					   }
				   }
			      return FileVisitResult.CONTINUE;
			   }
			});
			
    	} catch (IOException e) {
    		e.printStackTrace();
    	} 
    	
	}


	/**
	 * Insert timestamp of pulling files from particular path for a specific data type
	 * @param type - data type
	 * @param source - source dir (top path)
	 * @param current - timestamp of the latest pull.
	 */
	public static void insertFileLocationTB(String type, String source, DateTime current) {
		String typeCode = TransferUtils.convertTypeStr(type);
		
		Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        // get filetype ID by filetype code from FILE_TYPE_TB
        String hql = "FROM hibernate.FileType FT WHERE FT.code = '" + typeCode + "'";
        Query query = session.createQuery(hql);
        // only retrieve one object
        query.setMaxResults(1);
        FileType ft = (FileType) query.uniqueResult();
        
        // insert record into FILE_LOCATION_TB with filetype ID
        FileLocation fLoc = new FileLocation(ft.getId(), source, current, current, current);
        fLoc.setType("SRC");
        session.save(fLoc);
        session.getTransaction().commit();
        HibernateUtil.shutdown();
		
	}


}