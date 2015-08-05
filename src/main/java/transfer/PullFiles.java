package transfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dao.AuditTable;
import dao.OracleDB;
import rest.PushFiles;

public class PullFiles {
	
//	final static String DEST_ROOT = "/Users/djiao/Work/moonshot/dest";
	final static String DEST_ROOT = "/rsrch1/rists/moonshot";
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	static int fileCounter = 0;
	final static Connection CONN = OracleDB.getConnection();
	
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
		    	tmpInsert = AuditTable.insertMulti(CONN, insertLog);
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

//		    source = "/Users/djiao/Work/moonshot/vcf";
//		    cpFiles(source, DEST, TYPE, UPDATE, PROTOCOL, logWriter, insertWriter);
		    Map<String, String> env = System.getenv();
		    for (String envName : env.keySet()) {
		    	if (envName.contains("SOURCE_DIR")) {
		    		source = env.get(envName);
		    		if (source.length() > 3) {
		    			if (type.equals("mapping")) {
		    				Runtime.getRuntime().exec("rsync -auv djiao@" + source + " " + DEST);
		    			    processMappingFiles(DEST, insertWriter, logWriter);
		    			}
		    			else {
		    				if (new File(source).isDirectory())
				    			cpFiles(source, DEST, type, update, logWriter, insertWriter);
				    		else
				    			System.err.println("Source Dir " + envName + "(" + source + ")" + " is not a directory.");
		    			}
		    		}	
		    	}
		    }
		    logWriter.close();
		    insertWriter.close();
		    // rename log if not empty, otherwise delete it
		    if (Files.size(logfile.toPath()) > 0) {
		    	File newlog = new File(LOGPATH, "pull_" + dtStr + ".log");
		    	logfile.renameTo(newlog);
		    }
		    else {
		    	logfile.delete();
		    }
		    
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
	 */
	private static void processMappingFiles(String dest, PrintWriter insertWriter, PrintWriter logWriter) {
		
	}


	public static void counterMethod(){
		fileCounter++;
	}
	
	public static void cpFiles(String source, String dest, String type, String update, PrintWriter logWriter, PrintWriter insertWriter) {
		
    	Path top = Paths.get(source);
    	final String TYPE = type;
    	final String UPDATE = update;
    	String protocol = "";
    	if (type.equals("vcf") || type.equals("cnv") || type.equals("exon") || type.equals("gene")) 
    		protocol = "ln";
    	else
    		protocol = "cp";
    	final String PROTOCOL = protocol;
    	final String DEST = dest;
    	final PrintWriter LOG = logWriter;
    	final PrintWriter INSERT = insertWriter;
    	System.out.println("In method" + PROTOCOL);
    	
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
						   System.out.println(cmd);
						   Runtime.getRuntime().exec(cmd);
						   if (TYPE.equals("immunopath")) {
							   newName = switchExt(newName, "tsv");
							   toPath = Paths.get(DEST, newName);
							   FileConvert.immunoTsv(oldPath.toFile(), toPath.toFile());
						   }
						   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), PROTOCOL) == 0) {
							   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
						   }
						   LOG.println(newName + "\t" + srcPath + "\t" + DEST);
						   System.out.println(newName);
						   counterMethod();
					   }
					   else {  // add only new files
						   File lastLog = PushFiles.lastPullLog(DEST + "/logs");
						   if (lastLog == null) {
							   Runtime.getRuntime().exec(cmd);
							   if (TYPE.equals("immunopath")) {
								   newName = switchExt(newName, "tsv");
								   toPath = Paths.get(DEST, newName);
								   FileConvert.immunoTsv(oldPath.toFile(), toPath.toFile());
							   }
							   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), PROTOCOL) == 0) {
								   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
							   }
							   LOG.println(newName + "\t" + srcPath + "\t" + DEST);
							   System.out.println(newName);
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
									   FileConvert.immunoTsv(oldPath.toFile(), toPath.toFile());
								   }
								   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), PROTOCOL) == 0) {
									   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
								   }
								   LOG.println(newName + "\t" + srcPath + "\t" + DEST);
								   System.out.println(newName);
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
		if (type.equalsIgnoreCase("mapping")) {
			if (filename.toLowerCase().startsWith("crossreference")) {
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
	
	protected static String switchExt(String filename, String ext) {
		int stop = filename.lastIndexOf(".");
		String base = filename.substring(0, stop);
		return base + "." + ext;
	}


}