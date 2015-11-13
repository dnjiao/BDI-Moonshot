package org.mdacc.rists.bdi.transfer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.joda.time.DateTime;
import org.mdacc.rists.bdi.dbops.DBConnection;
import org.mdacc.rists.bdi.dbops.FileQueueUtil;

public class PushFiles {
	final static String URL_STRING = "http://10.111.100.207:8098/bdi/serviceingestion?domain=";
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Invalid arguments.Usage: PushFiles [bool]");
			System.exit(1);
		}
		boolean pushFlag = Boolean.parseBoolean(args[0]);
		String type = System.getenv("TYPE");
		String prefix = URL_STRING  + type + "&fileName=";
		
		
		Connection conn = DBConnection.getConnection();
		try {
			
	        // call stored procedure to get unsent files by type
			ResultSet rs = FileQueueUtil.getUnsent(conn, type);
			
			// counter of successfully pushed files
			int rowcount = 0;
			DateTime dt;
			
			// loop thru results
			while (rs.next()) {
				int rowId = rs.getInt("ROW_ID");
				String filepath = rs.getString("FILE_URI");
				dt = new DateTime();
				if (pushSingle(prefix, filepath, pushFlag) == 1) {
					FileQueueUtil.updateSendStatus(conn, rowId, dt);
					rowcount ++;
				}
			}
			
			System.out.println("Total of " + Integer.toString(rowcount) + " files pushed.");
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
	 * @param filepath - Path of local file to be uploaded
	 * @param ifReal - boolean flag for real/fake push
	 */
	public static int pushSingle(String prefix, String filepath, boolean ifReal) {
		// fake push for testing purpose
		if (ifReal == false) {
			System.out.println(filepath + " pushed (mock).");
			return 1;
		}
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