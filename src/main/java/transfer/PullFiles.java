package transfer;

import java.io.File;
import java.io.FileOutputStream;
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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dao.AuditTable;
import dao.OracleDB;
import rest.PushFiles;

public class PullFiles {
	
	final static String DEST_ROOT = "/rsrch1/rists/moonshot";
	final static DateTimeFormatter FORMAT = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	static int fileCounter = 0;
	final static Connection CONN = OracleDB.getConnection();
	
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
	    final String UPDATE = System.getenv("MODE").toLowerCase();
	    if (TYPE == null || UPDATE == null) {
	    	System.out.println("ERROR: Environment variable not set correctly.");
	    	System.exit(1);
	    }

	    final String DEST = DEST_ROOT + "/" + TYPE;
	    final String LOGPATH = DEST + "/logs";
	    String source;
	    File destDir = new File(DEST);
	    if (!destDir.exists()){
	        System.err.println("ERROR: Destination path " + DEST + " does not exist.");
	        System.exit(1);
	    }
	    File logDir = new File(LOGPATH);
	    try {
		    if (!logDir.exists()) {
				Files.createDirectory(Paths.get(LOGPATH));
		    }
		    File insertLog = new File(LOGPATH, "failed2insert.log");
		    File tmpInsert = null;
		    
		    // if failed2insert.log exist, insert records from last time to database
		    if (insertLog.exists()) {
		    	tmpInsert = AuditTable.insertMulti(CONN, insertLog);
		    }
		    else {
		    	tmpInsert = new File(LOGPATH, "tmp_insert.log");
		    }
		    Files.copy(tmpInsert.toPath(), insertLog.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
			    		if (new File(source).isDirectory())
			    			cpFiles(source, DEST, TYPE, UPDATE, logWriter, insertWriter);
			    		else
			    			System.err.println("Source Dir " + envName + "(" + source + ")" + " is not a directory.");
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
		    System.out.println("Total " + Integer.toString(fileCounter) + " " + TYPE + " files transferred successfully.");
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void counterMethod(){
		fileCounter++;
	}
	
	public static void cpFiles(String source, String dest, String type, String update, PrintWriter logWriter, PrintWriter insertWriter) {
		
    	Path top = Paths.get(source);
    	final String TYPE = type;
    	final String UPDATE = update;
    	final String DEST = dest;
    	final PrintWriter LOG = logWriter;
    	final PrintWriter INSERT = insertWriter;
    	
    	try {
			Files.walkFileTree(top, new SimpleFileVisitor<Path>()
			{  
			   @Override
			   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
			   {
				   String fileName = filePath.getFileName().toString();
				   DateTime now = new DateTime();
				   String newName = fileName.split(".")[0] + "_" + FORMAT.print(now) + "." + fileName.split(".")[1];
				   if (isType(fileName, TYPE)) {
					   String srcPath = filePath.getParent().toString();
					   
					   Path fromPath = filePath;
					   Path toPath = Paths.get(DEST, newName);
					   if (UPDATE.equalsIgnoreCase("all")) {  // add all files
						   Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
						   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), "cp") == 0) {
							   INSERT.println(fromPath.toString() + "\t" + toPath.toString());
						   }
						   LOG.println(newName + "\t" + srcPath + "\t" + DEST);
						   System.out.println(newName);
						   counterMethod();
					   }
					   else {  // add only new files
						   File lastLog = PushFiles.lastPullLog(DEST + "/logs");
						   if (lastLog == null) {
							   Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
							   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), "cp") == 0) {
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
								   Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
								   if (AuditTable.insertSingle(CONN, fromPath.toString(), toPath.toString(), "cp") == 0) {
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
		return false;
	}

}