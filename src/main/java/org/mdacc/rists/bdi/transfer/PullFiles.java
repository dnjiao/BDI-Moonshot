package org.mdacc.rists.bdi.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
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
import org.mdacc.rists.bdi.dbops.FileQueueUtil;
import org.mdacc.rists.bdi.dbops.FileTransferAuditUtil;
import org.mdacc.rists.bdi.dbops.DBConnection;
import org.mdacc.rists.bdi.hibernate.FileLocation;
import org.mdacc.rists.bdi.hibernate.FileType;
import org.mdacc.rists.bdi.hibernate.HibernateUtil;

public class PullFiles {
	
	final static String DEST_ROOT = "/rsrch1/rists/moonshot/data/stg";
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	static int fileCounter = 0;
	static List<String> dirs = new ArrayList<String>();
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
	    try {
		    // get the string for current time
		    DateTime current = new DateTime();
		    if (type.equals("mapping")) {
				// call bash script to transfer mapping files by sftp
				try {
					String[] cmd = new String[]{"/bin/bash", "/rsrch1/rists/moonshot/apps/sh/sftp.sh", DEST};
					source = "Informat";
					
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
					fileCounter = processMappingFiles(source, DEST, current);
					in.close();
					err.close();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		    else {
			    Map<String, String> env = System.getenv();
			    for (String envName : env.keySet()) {
			    	if (envName.contains("SOURCE_DIR")) {
			    		source = env.get(envName);
			    		System.out.println("source: " + source);
			    		if (source.length() > 3) {
			    			if (type.equals("flowcyto")) {
			    				processFlowFiles(source, DEST);
			    				FileLocationUtil.setLastTimeStamp(CONN, "mapping", "Informat server", current);
			    			}
			    			else {
			    				if (new File(source).isDirectory()) {
			    					cpFiles(source, DEST, type, current);
			    					for (String d : dirs) {
			    						System.out.println("Dir: " + d);
			    						FileLocationUtil.setLastTimeStamp(CONN, type, d, current);
			    					}
			    				}
					    			
					    		else
					    			System.err.println("Source Dir " + envName + "(" + source + ")" + " is not a directory.");
			    			}
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
		DateTime lastTS = FileLocationUtil.getLastTimeStamp(CONN, "mapping", source);
		System.out.print("last_ts for mapping: ");
		System.out.println(lastTS);
		File destDir = new File(dest);
		int fileCounter = 0;
		List<String> files;
		
		for (File file : destDir.listFiles()) {
			if (file.isDirectory() == false) { // ignore directories
				if (TransferUtils.isMapping(file)) {
					System.out.println(file.getName() + " last modified ts: " + FORMAT.print(file.lastModified()));
					if (lastTS == null || (lastTS != null && lastTS.isBefore(file.lastModified()))) {
						files = new ArrayList<String>();
						String fileName = file.getName();
						String newName = fileName.split("\\.")[0] + "_" + FORMAT.print(current) + "." + fileName.split("\\.")[1];
						File newfile = new File(file.getParent(), newName);
						removeReturnChar(file, newfile);
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
				}
			}
			else {
				file.delete();
			}
		}
		
		return fileCounter;
	}
	
	private static void removeReturnChar(File oldfile, File newfile) {
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


	public static void counterMethod(){
		fileCounter++;
	}
	
	private static void addDirs(String dirPath) {
		if (! dirs.contains(dirPath)) 
			dirs.add(dirPath);
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
						   FileTransferAuditUtil.insertRecord(CONN, dir.toString(), outFile, "Process");
					   }
				   }
				   return FileVisitResult.CONTINUE;
			   }
			});
				
    	} catch (IOException e) {
    		e.printStackTrace();
    	} 
	}


	public static void cpFiles(String source, String dest, String type, DateTime current) {		
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
					   String srcPath = file.getParent();
					   DateTime lastDt = FileLocationUtil.getLastTimeStamp(CONN, TYPE, srcPath);
					   if (lastDt == null || (lastDt != null && lastDt.isBefore(file.lastModified()))) {
						   String newName = fileName.split("\\.")[0] + "_" + FORMAT.print(CURRENT) + "." + fileName.split("\\.")[1];
						   Path fromPath = filePath;
						   Path toPath = Paths.get(DEST, newName);
						   Path oldPath = toPath;
						   String cmd = "cp " + fromPath.toString() + " " + toPath.toString();				  
						   List<String> files = new ArrayList<String>();
						   Runtime.getRuntime().exec(cmd);
						   addDirs(srcPath);
						   FileTransferAuditUtil.insertRecord(CONN, fromPath.toString(), toPath.toString(), "cp");
						   if (TYPE.equals("immunopath")) {
							   newName = TransferUtils.switchExt(newName, "tsv");
							   toPath = Paths.get(DEST, newName);
							   FileConversion.immunoTsv(oldPath.toFile(), toPath.toFile());
						   }
						   int fileQueueId = FileQueueUtil.insertRecord(CONN, toPath.toString(), TYPE);
						   counterMethod();
						   files.add(toPath.toString());
						   FileTransferAuditUtil.updateFileQueueId(CONN, files, fileQueueId);
							   
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