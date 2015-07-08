package rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	final static String URL_STRING = "http://10.111.100.207:8098/bdi/serviceingestion?domain=";
	final static String LOCAL_PATH = "/rsrch1/rists/moonshot";
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: PushFile [File_Domain]");
			System.exit(1);
		}
		FileOutputStream outstream = null;
		PrintStream ps = null;
		try {
			String url = URL_STRING  + args[0];
			String path = LOCAL_PATH + "/" + args[0];
			
			List<String> files = getFiles(path);
			File notSentFile = new File(path + "/logs/not_sent.log");
			outstream = new FileOutputStream(notSentFile, false);
			ps = new PrintStream(outstream);
			int sentCount = 0;
			int notsentCount = 0;
			
			for (String filename : files) {
				String filepath = path + "/" + filename;
				if (pushSingle(url, filepath) == 0) { // not sent successfully
					notsentCount ++;
					ps.print(filename);
					ps.println();
				} 
				else {
					sentCount ++;
				}
			}
			ps.close();
			outstream.close();
			// if all files sent, delete "not_sent.log"
			if (notsentCount == 0) {
				Path p = Paths.get(path);
			    try {
					Files.delete(p);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
			
			System.out.println(Integer.toString(sentCount) + " files have been uploaded.");
		} catch(IOException e) {
			e.printStackTrace();
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
		File[] logs = dir.listFiles();
		DateTimeFormatter format = DateTimeFormat.forPattern("ddMMyyyyHHmmss");
		DateTime last = format.parseDateTime("01012000000000");
		for (File file : logs) {
			String filename = file.getName();
			if (filename.startsWith("pull") && filename.endsWith(".log")) {
				String timeStr = filename.split(".log")[0];
				DateTime time = format.parseDateTime(timeStr);
				if (time.isAfter(last)) {
					last = time;
				}
			}
		}
		String timeStr = last.toString(format);
		File file = new File(path, "pull" + timeStr + ".log");
		return file;
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