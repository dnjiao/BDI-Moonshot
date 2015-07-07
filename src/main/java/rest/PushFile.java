package rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class PushFile {
	final static String URL_STRING = "http://10.111.100.207:8098/bdi/serviceingestion?domain=";
	final static String LOCAL_PATH = "/rsrch1/rists/moonshot";
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: PushFile [File_Domain]");
			System.exit(1);
		}
		String url = URL_STRING  + args[0];
		String path = LOCAL_PATH + "/" + args[0];
		List<File> files = getFiles(path);
		
		int fileCount = 0;
		
		for (File f : files) {
			try {
				fileCount += pushSingle(url, f.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(Integer.toString(fileCount) + " files have been uploaded.");
	}
	
	/**
	 * Obtain a list of files to be sent
	 * @param path - path to the local file directory
	 * @return list of files
	 */
	public static List<File> getFiles(String path) {
		List<File> files = new ArrayList<File>();
		String logPath = path + "/logs";
		File log = lastLog(logPath);
	}
	
	/**
	 * Get the log that is last generated
	 * @param path - Log path
	 * @return latest log file
	 */
	public static File lastLog(String path) {
		File dir = new File(path);
		File[] logs = dir.listFiles();
		for (File file : logs) {
			String filename = file.getName();
			if (filename.endsWith(".log")) {
				String timeStr = filename.split(".log")[0];
				DateTimeFormatter format = DateTimeFormat.forPattern("'T'HH:mm:ss.SSSXXX");
				DateTime time = format.parseDateTime("2013-09-18T20:40:00+0000");
			}
		}
	}
	
	/**
	 * call Restful service and push single file
	 * @param url - RestFul URL (destination)
	 * @param filepath - Path of local file to be uploaded
	 */
	public static int pushSingle(String url, String filepath) {
		int status = 500;
		PostMethod filePost = null;
		try {
			//From Directory or File we need to pick files
			File f = new File(filepath);
			//Need to validate the file name from the file name
			filePost = new PostMethod(url);
			RequestEntity re = new FileRequestEntity(f,	"application/octet-stream");
			filePost.setRequestEntity(re);
			HttpClient client = new HttpClient();
			long start = System.currentTimeMillis();
			status = client.executeMethod(filePost);
			System.out.println("Time taken for execution " + (System.currentTimeMillis() - start) + "ms");
			return 1;
		} catch (HttpException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} 
		finally {
			filePost.releaseConnection();
		}

	}
	
}