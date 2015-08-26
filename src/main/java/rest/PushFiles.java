package rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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

public class PushFiles {
//	final static String URL_STRING = "http://10.111.100.207:8098/bdi/serviceingestion?domain=";
	final static String URL_STRING = "http://10.113.241.42:8099/bdi/serviceingestion?domain=";
	final static String LOCAL_PATH = "/rsrch1/rists/moonshot/data/dev";
	
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
		String prefix = URL_STRING  + TYPE + "&fileName=";
		String path = LOCAL_PATH + "/" + TYPE;
		PrintWriter writer = null;
		try {
			List<String> files = getFiles(path);
			System.out.println(files.size());
			File notSentLog = new File(path + "/logs/not_sent.log");
			writer = new PrintWriter(notSentLog);
			int sentCount = 0;
			int notsentCount = 0;
			
			for (String filename : files) {
				String filepath = path + "/" + filename;
				if (pushSingle(prefix, filepath) == 0) { // not sent successfully
					notsentCount ++;
					writer.println(filename);
				} 
				else {
					sentCount ++;
				}
			}
			// if all files sent, delete "not_sent.log"
			if (notsentCount == 0) {
				Path p = notSentLog.toPath();
			    try {
					Files.delete(p);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
			System.out.println(Integer.toString(sentCount) + " files have been pushed to BDI.");
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}
	
	/**
	 * Obtain a list of files to be sent
	 * @param path - path to the local file directory
	 * @return list of files
	 * @throws IOException 
	 */
	public static List<String> getFiles(String path) throws IOException {
		List<String> files = new ArrayList<String>();
		String logPath = path + "/logs";
		File pullLog = lastPullLog(logPath);
		BufferedReader br = new BufferedReader(new FileReader(pullLog));
		String line = null;
		while ((line = br.readLine()) != null) {
			files.add(line.split("\t")[0]);
		}
		br.close();
		File fp = new File(path, "not_sent.log");
		if (!fp.exists()) {
			return files;
		}
		br = new BufferedReader(new FileReader(fp));
		String linestr;
		while ((line = br.readLine()) != null) {
			linestr = line.replaceAll("(\\r|\\n)", "");
			if (!files.contains(linestr)) {
				files.add(linestr);
			}
		}
		br.close();
		return files;
		
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
	 * call Restful service and push single file
	 * @param url - RestFul URL (destination)
	 * @param filepath - Path of local file to be uploaded
	 */
	public static int pushSingle(String prefix, String filepath) {
//		return 1;
		int status = 500;
		PostMethod filePost = null;
		try {
			//From Directory or File we need to pick files
			File f = new File(filepath);
			String fileName = f.getName();
			//Need to validate the file name from the file name
			String url = prefix + fileName.substring(0,fileName.lastIndexOf("."));
			System.out.println("PostMethod URL: " + url);
			filePost = new PostMethod(url);
			RequestEntity re = new FileRequestEntity(f,	"application/octet-stream");
			filePost.setRequestEntity(re);
			
			// hard timeout after 15 sec
			int timeout = 15;
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout * 1000); 
			client.getHttpConnectionManager().getParams().setSoTimeout(timeout * 1000);
			status = client.executeMethod(filePost);
			
			if (status == 201) {
				System.out.println(filepath + " uploaded to " + url + ". Status=" + Integer.toString(status));
				return 1;
			}
			else if (status == 404) {
				System.out.println("RESOURCE NOT FOUND,Check the request path. Status= " + status);
				return 0;
			}
			else if (status == 405) {
				System.out.println("RESOURCE METHOD NOT FOUND,Check the request path. Status= "	+ status);
				return 0;
			}
			else if (status == 500) {
				System.out.println("INTERNAL SERVER ERROR. Status= " + status);
				return 0;
			}
			else if (status == 504 || status == 404) {
				System.out.println("Time out. Status= " + status);
				return 0;
			}
			else {
				System.out.println("Status=" + status);
				return 0;
			}
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