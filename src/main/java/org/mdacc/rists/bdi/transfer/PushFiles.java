package org.mdacc.rists.bdi.transfer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.joda.time.DateTime;
import org.mdacc.rists.bdi.dbops.DBConnection;
import org.mdacc.rists.bdi.dbops.FileQueueUtil;

public class PushFiles {
	final static String URL_STRING = "http://10.113.241.55:8099/bdi/serviceingestion?domain=";
	final static String USERNAME = "ristsvc";
	final static String PASSWORD = "CH!M@321";
	static List <String> TYPES = Arrays.asList("vcf", "cnv", "exon", "gene", "splice");
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Invalid arguments.Usage: PushFiles [type] [bool]");
			System.exit(1);
		}
		String type = args[0].toLowerCase();
		if (!TYPES.contains(type)) {
			System.err.println("Invalid type to push: " + type);
			System.exit(1);
		}
		boolean pushFlag = Boolean.parseBoolean(args[1]);
		String prefix = URL_STRING  + type + "&fileName=";
		
		Connection conn = DBConnection.getConnection();
		try {
			
	        // call stored procedure to get unsent files by type
			ResultSet rs = FileQueueUtil.getUnsent(conn, type);
			
			// counter of successfully pushed files
			int rowcount = 0;
			DateTime dt;
			if (rs == null) {
				System.out.println("No " + type + " files to push.");
				return;
			}
			// loop thru results
			while (rs.next()) {
				int rowId = rs.getInt("ROW_ID");
				String filepath = rs.getString("FILE_URI");
				dt = new DateTime();
				if (pushSingle(prefix, USERNAME, PASSWORD, filepath, pushFlag) == 1) {
					FileQueueUtil.updateSendStatus(conn, rowId, dt);
					rowcount ++;
				}
			}
			
			System.out.println("Total of " + Integer.toString(rowcount) + " " + type + " files pushed.");
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}

	
	/**
	 * call Restful service and push single file
	 * @param url - RestFul URL (destination)
	 * @param username - username of post service
	 * @param password - password for posting
	 * @param filepath - Path of local file to be uploaded
	 * @param ifReal - boolean flag for real/fake push
	 */
	public static int pushSingle(String prefix, String username, String password, String filepath, boolean ifReal) {
		// fake push for testing purpose
		if (ifReal == false) {
			System.out.println(filepath + " pushed (mock).");
			return 1;
		}
		int status = 500;
		HttpPost post = null;
		try {
			//From Directory or File we need to pick files
			File file = new File(filepath);
			String fileName = file.getName();

			String url = prefix + fileName;
			
			post = new HttpPost(url);
			// set username/password and content-type for posting
			post.setHeader("username", username);
			post.setHeader("password", password);
		
			FileEntity entity = new FileEntity(file, "application/octet-stream");   
			post.setEntity(entity);

			// hard timeout after 30 sec
			int timeout = 30;
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
		    client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);
			HttpResponse response = client.execute(post);
			status = response.getStatusLine().getStatusCode();
			
			if (status == 201) {
				System.out.println(filepath + " uploaded to " + url + ". Status=" + Integer.toString(status));
				return 1;
			}
			else if (status == 401) {
				System.out.println("AUTHORIZATION FAILED, Check the credentials. Status= " + status);
				return 0;
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
			else if (status == 504) {
				System.out.println("Time out. Status= " + status);
				return 0;
			}
			else {
				System.out.println("Status=" + status);
				return 0;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} 

	}
	
}