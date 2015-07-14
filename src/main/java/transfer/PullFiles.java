package transfer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rest.PushFiles;

public class PullFiles {
	
	final static String DEST_ROOT = "/rsrch1/rists/moonshot";
	static int fileCounter = 0;
	
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
	    final String UPDATE = System.getenv("MODE").toLowerCase();
	    if (TYPE == null || UPDATE == null) {
	    	System.out.println("ERROR: Environment variable not set correctly.");
	    	System.exit(1);
	    }
//		final String TYPE = "vcf";
//		final String UPDATE = "Update new";
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
		    
		    // get the string for current time
		    DateTimeFormatter format = DateTimeFormat.forPattern("MMddyyyyHHmmss");
		    DateTime current = new DateTime();
		    String dtStr = format.print(current);
		    
		    // open log file to write
		    File logfile = new File(LOGPATH, "tmp.log");
		    PrintWriter writer=new PrintWriter(logfile);
//		    String source = "/Users/djiao/Work/moonshot/vcf";
//		    if (source.length() != 1) {
//	    		cpFiles(source, DEST, TYPE, UPDATE, writer);
//	    	}
		    
		    Map<String, String> env = System.getenv();
		    for (String envName : env.keySet()) {
		    	if (envName.contains("SOURCE_DIR")) {
		    		source = env.get(envName);
		    		if (source.length() > 3) {
			    		if (new File(source).isDirectory())
			    			cpFiles(source, DEST, TYPE, UPDATE, writer);
			    		else
			    			System.err.println("Source Dir " + envName + "(" + source + ")" + " is not a directory.");
		    		}
		    			
		    	}
		    }
		    writer.close();
		    // rename log if not empty, otherwise delete it
		    if (Files.size(logfile.toPath()) > 0) {
		    	File newlog = new File(LOGPATH, "pull_" + dtStr + ".log");
		    	logfile.renameTo(newlog);
		    }
		    else {
		    	logfile.delete();
		    }
		    System.out.println("Total " + Integer.toString(fileCounter) + " " + TYPE + " files transferred successfully.");
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void counterMethod(){
		fileCounter++;
	}
	
	public static void cpFiles(String source, String dest, String type, String update, PrintWriter writer) {
		
    	Path top = Paths.get(source);
    	final String TYPE = type;
    	final String UPDATE = update;
    	final String DEST = dest;
    	final PrintWriter LOG = writer;
    	
    	try {
//					final Connection CONN = OracleDB.getConnection();
    		 
			Files.walkFileTree(top, new SimpleFileVisitor<Path>()
			{  
			   @Override
			   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException
			   {
				   String fileName = filePath.getFileName().toString();
				   if (isType(fileName, TYPE)) {
					   String srcPath = filePath.getParent().toString();
					   
					   Path fromPath = filePath;
					   Path toPath = Paths.get(DEST, fileName);
					   if (UPDATE.equalsIgnoreCase("all")) {  // add all files
						   Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
						   LOG.println(fileName + "\t" + srcPath + "\t" + DEST);
						   System.out.println(fileName);
						   counterMethod();
					   }
					   else {  // add only new files
						   File lastLog = PushFiles.lastPullLog(DEST + "/logs");
						   if (lastLog == null) {
							   Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
							   LOG.println(fileName + "\t" + srcPath + "\t" + DEST);
							   System.out.println(fileName);
							   counterMethod();
						   }
						   else {
							   String timeStr = lastLog.getName().split(".log")[0].split("_")[1];
							   DateTimeFormatter format = DateTimeFormat.forPattern("MMddyyyyHHmmss");
							   DateTime logTime = format.parseDateTime(timeStr);
							   // compare last log time and file lastmodified time
							   if (logTime.isBefore(fromPath.toFile().lastModified())) {
								   Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
								   LOG.println(fileName + "\t" + srcPath + "\t" + DEST);
								   System.out.println(fileName);
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