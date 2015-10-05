package org.mdacc.rists.bdi.dbops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

public class FileTransferAuditUtil {
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
			System.out.println("Return code: " + ret);
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
			ArrayDescriptor des = ArrayDescriptor.createDescriptor("SchemaName.ARRAY_TABLE", con);
			ARRAY array = new ARRAY(des, con, fileArray);
			CallableStatement stmt = con.prepareCall("{call FILE_TRANSFER_AUDIT_UTIL.update_file_queue_id(?,?)}");
			stmt.setArray(1, array);
			stmt.setInt(2, fileQueueId);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
}