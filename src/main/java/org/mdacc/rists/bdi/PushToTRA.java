package org.mdacc.rists.bdi;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.mdacc.rists.bdi.db.utils.DBConnection;
import org.mdacc.rists.bdi.db.utils.FileQueueUtil;
import org.mdacc.rists.bdi.db.utils.FileSendUtil;
import org.mdacc.rists.bdi.vo.FileQueueVO;

public class PushToTRA {

	public static void main(String[] args) throws SQLException {
	
	}
	
	public static void pushFilesByType(String type, String url, String username, String password, int conId, int typeId, String bool) {
		
		try {
			Connection conn = DBConnection.getConnection();
			String prefix = url + "&fileName=";
			List<FileQueueVO> fqList = FileQueueUtil.getUnsent(conn, type, "TRA");
			if (fqList == null) {
				System.out.println("No " + type + " files to push.");
				return;
			}
			
			// counter of successfully pushed files
			int successCount = 0;
			int failCount = 0;
			
			// loop thru results
			for (FileQueueVO fq : fqList) {
				int fqId = fq.getRowId();
				String filepath = fq.getFileUri();
				if (pushSingle(prefix, username, password, filepath, bool) == 1) {
					FileSendUtil.insertRecord(conn, "S", filepath, fqId, typeId, conId);
					successCount ++;
				}
				else {
					FileSendUtil.insertRecord(conn, "E", filepath, fqId, typeId, conId);
					System.out.println("Sending " + filepath + " failed.");
					failCount ++;
				}
			}
			
			System.out.println("Total of " + Integer.toString(successCount) + " " + type + " files pushed.");
			System.out.println("Total of " + Integer.toString(failCount) + " " + type + " files failed to Push.");
		
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
	public static int pushSingle(String prefix, String username, String password, String filepath, String ifReal) {
		
		File file = new File(filepath);
		String fileName = file.getName();
		String url = prefix + fileName;
		// fake push for testing purpose
		if (ifReal.equalsIgnoreCase("fake")) {
			System.out.println("Sending (mock):" + url);
			return 1;
		}
		int status = 500;
		HttpPost post = null;
		HttpClient client = new DefaultHttpClient();
		try {
			//From Directory or File we need to pick files

			System.out.println("Sending file: " + url);
			
			post = new HttpPost(url);
			// set username/password and content-type for posting
			post.setHeader("username", username);
			post.setHeader("password", password);
		
			FileEntity entity = new FileEntity(file, "application/octet-stream");   
			post.setEntity(entity);

			// hard timeout after 30 sec
			int timeout = 30;
			
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout * 1000);
		    client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout * 1000);
			HttpResponse response = client.execute(post);
			status = response.getStatusLine().getStatusCode();
			client.getConnectionManager().shutdown();
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
			client.getConnectionManager().shutdown();
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			client.getConnectionManager().shutdown();
			return 0;
		} 

	}
	
}