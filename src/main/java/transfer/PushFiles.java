package transfer;

import hibernate.FileQueue;
import hibernate.HibernateUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleTypes;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import db_op.OracleDB;

public class PushFiles {
//	final static String URL_STRING = "http://10.113.241.42:8099/bdi/serviceingestion?domain=";
	final static String URL_STRING = "http://10.111.100.207:8098/bdi/serviceingestion?domain=";
	final static String LOCAL_PATH = "/rsrch1/rists/moonshot/data/dev";
	
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
		String prefix = URL_STRING  + TYPE + "&fileName=";
		boolean pushFlag = Boolean.parseBoolean(args[0]);
		
		
		Connection conn = OracleDB.getConnection();
		try {
			
			// create Hibernate session and Transaction
			Session session = HibernateUtil.getSessionFactory().openSession();
	        Transaction ts = session.beginTransaction();
	        
	        // call stored procedure
			CallableStatement pstmt = conn.prepareCall("{call FILE_PROCESS.get_untransferred_file_by_type(?,?)}");
			pstmt.setString(1, PullFiles.convertTypeStr(TYPE));
			pstmt.registerOutParameter(2, OracleTypes.CURSOR);
			pstmt.executeUpdate();
			
			// get cursor and cast it to ResultSet
			ResultSet rs = (ResultSet) pstmt.getObject(2);
			
			// counter of successfully pushed files
			int rowcount = 0;
			
			// loop thru results
			while (rs.next()) {
				int rowId = rs.getInt("ROW_ID");
				String filepath = rs.getString("DEST_FILE_URI");
				if (pushSingle(prefix, filepath, pushFlag) == 1) {
					rowcount ++;
				}
				FileQueue fq = (FileQueue)session.get(FileQueue.class, rowId);
				fq.setStatus("Y");
				session.update(fq);
			}
			ts.commit();
			session.close();
			System.out.println("Total of " + Integer.toString(rowcount) + " files pushed.");
			
			pstmt.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		catch (HibernateException e) {
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
			System.out.println(filepath + "pushed (mock).");
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