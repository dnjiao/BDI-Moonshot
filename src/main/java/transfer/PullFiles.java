package transfer;

import hibernate.FileLocation;
import hibernate.FileType;
import hibernate.HibernateUtil;
import imt_data.FileConversion;
import imt_data.FlowData;

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

import dao.AuditTable;
import dao.OracleDB;
import rest.PushFiles;

public class PullFiles {
	
//	final static String DEST_ROOT = "/Users/djiao/Work/moonshot/dest";
	final static String DEST_ROOT = "/rsrch1/rists/moonshot/data/dev";
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	static int fileCounter = 0;
	final static Connection CONN = OracleDB.getConnection();
	final static String PROTOCOL = System.getenv("PROTOCOL");
	
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
	    final String LOGPATH = DEST + "/logs";
	    String source;
	    File destDir = new File(DEST);
	    if (!destDir.exists()){
	        System.err.println("ERROR: Destination path " + DEST + " does not exist.");
	        System.exit(1);
	    }
	    File logDir = new File(LOGPATH);
	    String update;
	    try {
		    if (!logDir.exists()) {
				Files.createDirectory(Paths.get(LOGPATH));
				update = "all";
		    }
		    else {
		    	// find all pull logs if log directory exists
		    	File[] pullLogs = logDir.listFiles(new FilenameFilter() {
		    	    public boolean accept(File dir, String name) {
		    	        return name.startsWith("pull") && name.endsWith(".log");
		    	    }
		    	});
		    	// if no pull logs exist, pull all files
		    	if (pullLogs.length == 0)
		    		update = "all";
		    	// if pull log exists, pull only new files
		    	else
		    		update = "new";
		    }
		    File insertLog = new File(LOGPATH, "failed2insert.log");
		    File tmpInsert = null;
		    
		    // if failed2insert.log exist, insert records from last time to database
		    if (insertLog.exists()) {
		    	tmpInsert = AuditTable.insertMulti(CONN, insertLog, PROTOCOL);
		    }
		    else {
		    	tmpInsert = new File(LOGPATH, "tmp_insert.log");
		    	if (tmpInsert.exists()) {
		    		tmpInsert.delete();
		    	}
		    	tmpInsert.createNewFile();
		    }
		    Files.copy(tmpInsert.toPath(), insertLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
		    tmpInsert.delete();
		    PrintWriter insertWriter = new PrintWriter(new FileOutputStream(insertLog), true);
		    // get the string for current time
		    DateTime current = new DateTime();
		    String dtStr = FORMAT.print(current);
		    
		    // open log file to write
		    File logfile = new File(LOGPATH, "tmp_pull.log");
		    PrintWriter logWriter=new PrintWriter(logfile);

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
		    			    fileCounter = processMappingFiles(source, DEST, insertWriter);
		    			}
		    			else if (type.equals("flowcyto")) {
		    				processFlowFiles(source, DEST, insertWriter);
		    			}
		    			else {
		    				if (new File(source).isDirectory())
				    			cpFiles(source, DEST, type, update, insertWriter);
				    		else
				    			System.err.println("Source Dir " + envName + "(" + source + ")" + " is not a directory.");
		    			}
		    			insertFileLocationTB(type, source, current);
		    		}	
		    	}
		    }
		    insertWriter.close();
		  
		    // delete insert log if empty
		    if (Files.size(insertLog.toPath()) == 0) {
		    	insertLog.delete();
		    }
		    System.out.println("Total " + Integer.toString(fileCounter) + " " + type + " files transferred successfully.");
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
	private static int processMappingFiles(String source, String dest, PrintWriter insertWriter) {
		List<File> files = getNewMappingFiles(dest);
		for (File file : files) {
			System.out.println(file.getName());
			if (AuditTable.insertSingle(CONN, source + "/" + file.getName(), file.getAbsolutePath(), "sftp") == 0) {
				insertWriter.println(source + "\t" + file.getAbsolutePath());
			}
		}	
		return files.size();
	}


	/**
	 * get a list of  files that were last transfered
	 * @param dir - directory that contains all the files
	 * @return - list of files
	 */
	private static List<File> getNewMappingFiles(String dir) {
		List<File> newFiles = new ArrayList<File>();
		File dirFile = new File(dir);
		File[] files = dirFile.listFiles();
		File lastLog = lastPullLog(dir + "/logs");
		for (File file : files) {	
			if (file.isDirectory() == false) {
				// log does not exist, all files are new; exists, only files newer than last log
				if (lastLog == null) {
					if (isMapping(file))					
						newFiles.add(file);
					else
						file.delete();
				}
				else {
					String timeStr = lastLog.getName().split(".log")[0].split("_")[1];
					DateTime logTime = FORMAT.parseDateTime(timeStr);
					// compare last log time and file lastmodified time
					if (logTime.isBefore(file.lastModified())) {
						if (isMapping(file)) 
							newFiles.add(file);
						else
							file.delete();
					}
				}
			}
		}
		return newFiles;
	}
	
	/**
	 * Get the log that is last generated
	 * @param path - Log path
	 * @return latest log file
	 */
	public static File lastPullLog(String path) {
		File dir = new File(path);
		File[] logs = dir.listFiles(new FilenameFilter() {
    	    public boolean accept(File dir, String name) {
    	        return name.startsWith("pull") && name.endsWith(".log");
    	    }
    	});
		if (logs.length == 0) {  // no logs found
			return null;
		}
		DateTimeFormatter format = DateTimeFormat.forPattern("MMddyyyyHHmmss");
		DateTime last = format.parseDateTime("01012000000000");
		for (File file : logs) {
			String filename = file.getName();
			String timeStr = filename.substring(0, filename.lastIndexOf(".")).split("_")[1];
			DateTime time = format.parseDateTime(timeStr);
			if (time.isAfter(last)) {
				last = time;
			}
		}
		if (last == format.parseDateTime("01012000000000")) {
			return null;	
		}
		else {
			String timeStr = last.toString(format);
			File file = new File(path, "pull_" + timeStr + ".log");
			return file;
		}
	}

	/**
	 * determine if a file is mapping file based on first line text
	 * @param file - input file
	 * @return - boolean, true means mapping false means not
	 */
	private static boolean isMapping(File file) {
		boolean bool = false;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String firstline = reader.readLine();
			if (firstline.startsWith("Project|Subproject|Specimen")) 
				bool = true;
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
	
	/**
	 * Insert timestamp of pulling files from particular path for a specific data type
	 * @param type - data type
	 * @param source - source dir (top path)
	 * @param current - timestamp of the latest pull.
	 */
	public static void insertFileLocationTB(String type, String source, DateTime current) {
		String typeCode = convertTypeStr(type);
		
		Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        // get filetype ID by filetype code from FILE_TYPE_TB
        String hql = "FROM hibernate.FileType FT WHERE FT.code = '" + typeCode + "'";
        Query query = session.createQuery(hql);
        List results = query.list();
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
				return "RNASeq Junctions Counts";
			default:
				System.err.println("Invalid file type: " + type);
				return null;
		}
	}


	public static void counterMethod(){
		fileCounter++;
	}
	
	
	private static void processFlowFiles(String source, String dest, PrintWriter insertWriter) {
		Path top = Paths.get(source);
    	final String DEST = dest;
    	final PrintWriter INSERT = insertWriter;
    	
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
						   if (AuditTable.insertSingle(CONN, dir.toString(), outFile, "Process") == 0) {
							   System.out.println("not inserted " + dir.toString() + "\t" + outFile);
							   INSERT.println(dir.toString() + "\t" + outFile);
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


	public static void cpFiles(String source, String dest, String type, String update, PrintWriter insertWriter) {		
    	Path top = Paths.get(source);
    	final String TYPE = type;
    	final String UPDATE = update;
    	
    	final String DEST = dest;
    	final PrintWriter INSERT = insertWriter;
    	
    	try {
			Files.walkFileTree(top, new SimpleFileVisitor<Path>()
			{  
			   @Override
			   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
			   {
				   String fileName = filePath.getFileName().toString();
				   DateTime now = new DateTime();
				   
				   if (isType(fileName, TYPE)) {	
					   String newName = fileName.split("\\.")[0] + "_" + FORMAT.print(now) + "." + fileName.split("\\.")[1];
					   String srcPath = filePath.getParent().toString();
					   
					   Path fromPath = filePath;
					   Path toPath = Paths.get(DEST, newName);
					   Path oldPath = toPath;
					   String cmd = cmdConstructor(PROTOCOL, fromPath.toString(), toPath.toString());
					   if (UPDATE.equalsIgnoreCase("all")) {  // add all files
						   Runtime.getRuntime().exec(cmd);
						   if (TYPE.equals("immunopath")) {
							   newName = switchExt(newName, "tsv");
							   toPath = Paths.get(DEST, newName);
							   FileConversion.immunoTsv(oldPath.toFile(), toPath.toFile());
						   }
						   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), PROTOCOL) == 0) {
							   System.out.println("not inserted " + fromPath.toString() + "\t" + toPath.toString());
							   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
						   }
						   counterMethod();
					   }
					   else {  // add only new files
						   File lastLog = lastPullLog(DEST + "/logs");
						   if (lastLog == null) {
							   Runtime.getRuntime().exec(cmd);
							   if (TYPE.equals("immunopath")) {
								   newName = switchExt(newName, "tsv");
								   toPath = Paths.get(DEST, newName);
								   FileConversion.immunoTsv(oldPath.toFile(), toPath.toFile());
							   }
							   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), PROTOCOL) == 0) {
								   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
							   }
							   counterMethod();
						   }
						   else {
							   String timeStr = lastLog.getName().split(".log")[0].split("_")[1];
							   DateTime logTime = FORMAT.parseDateTime(timeStr);
							   // compare last log time and file lastmodified time
							   if (logTime.isBefore(fromPath.toFile().lastModified())) {
								   Runtime.getRuntime().exec(cmd);
								   if (TYPE.equals("immunopath")) {
									   newName = switchExt(newName, "tsv");
									   toPath = Paths.get(DEST, newName);
									   FileConversion.immunoTsv(oldPath.toFile(), toPath.toFile());
								   }
								   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), PROTOCOL) == 0) {
									   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
								   }
								   counterMethod();
							   }
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

	/**
	 * determine if a file is the type
	 * @param filename 
	 * @param type - file type, e.g. "vcf"
	 * @return true if a file is the type, false otherwise
	 */
	public static boolean isType(String filename, String type) {
		if (type.equalsIgnoreCase("vcf")) {
			if(filename.endsWith(".vcf")) {
				return true;
			}
		}
		if (type.equalsIgnoreCase("immunopath")) {
			if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
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


}