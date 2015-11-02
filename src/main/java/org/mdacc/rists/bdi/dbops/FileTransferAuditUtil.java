package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

public class FileTransferAuditUtil {
	public static void main(String[] args) {
		List<String> fileList = new ArrayList<String>();
		String f1 = "/rsrch1/rists/moonshot/data/stg/vcf/300.vcf";
		String f2 = "/rsrch1/rists/moonshot/data/stg/vcf/400.vcf";
		fileList.add(f1);
		fileList.add(f2);
		Connection con = DBConnection.getConnection();
		updateFileQueueId(con, fileList, 830);
//		String source = "source";
//		String dest = "/rsrch1/rists/moonshot/data/stg/vcf/400.vcf";
//		Connection con = DBConnection.getConnection();
//		insertRecord(con, source, dest, "cp");
	}
	public static int insertRecord (Connection con, String sourceUri, String destUri, String protocol) {
		int ret = 0;
		try {
			CallableStatement stmt = con.prepareCall("{call FILE_TRANSFER_AUDIT_UTIL.insert_record(?,?,?,?,?,?,?)}");
			stmt.setString(1, sourceUri);
			stmt.setString(2, destUri);
			stmt.setString(3, protocol.toUpperCase());
			stmt.registerOutParameter(4, Types.INTEGER);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.executeUpdate();
			ret = stmt.getInt(4);
			if (ret == 0) {
				System.out.println(stmt.getString(6));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return ret;
	}
	
	public static void updateFileQueueId (Connection con, List<String> fileList, int fileQueueId) {
		String[] fileArray = fileList.toArray(new String[fileList.size()]);
		try{
			ArrayDescriptor des = ArrayDescriptor.createDescriptor("VARCHAR2_TABLE", con);
			ARRAY array = new ARRAY(des, con, fileArray);
			CallableStatement stmt = con.prepareCall("{call FILE_TRANSFER_AUDIT_UTIL.update_file_queue_id(?,?,?,?,?,?)}");
			stmt.setArray(1, array);
			stmt.setInt(2, fileQueueId);
			stmt.registerOutParameter(3, Types.INTEGER);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
}