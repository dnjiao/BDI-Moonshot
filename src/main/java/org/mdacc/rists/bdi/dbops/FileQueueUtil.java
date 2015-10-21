package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import oracle.jdbc.OracleTypes;

import org.mdacc.rists.bdi.transfer.TransferUtils;

public class FileQueueUtil {
	
	public static void main(String[] args) {
		String type = "vcf";
		
		Connection con = DBConnection.getConnection();
		getUnsent(con,type);
		
	}
	public static ResultSet getUnsent (Connection con, String type) {
		CallableStatement stmt;
		ResultSet rs = null;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.get_unsent_file_by_type(?,?,?,?,?)}");
			stmt.setString(1, TransferUtils.convertTypeStr(type));
			stmt.registerOutParameter(2, OracleTypes.CURSOR);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.executeUpdate();
			
			// get cursor and cast it to ResultSet
			rs = (ResultSet) stmt.getObject(2);
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return rs;
	}
	
	public static int insertRecord (Connection con, String filepath, String type) {
		CallableStatement stmt;
		int queueId = 0;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.insert_record(?,?,?,?,?,?,?)}");
			stmt.setString(1, filepath);
			if (type.equals("vcf") || type.equals("cnv") || type.equals("rna")) {
				stmt.setString(2, null);
			}
			else {
				stmt.setString(2, "P");
			}
			stmt.registerOutParameter(3, Types.INTEGER);
			stmt.registerOutParameter(4, Types.INTEGER);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.executeUpdate();
			
			queueId = stmt.getInt(4);
			// if update/insert is not success, print out error description
			if (queueId == 0) {
				System.out.println("Error description: " + stmt.getString(6));
				System.out.println("Error trace: " + stmt.getString(7));
			}
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return queueId;
	}
	public static void updateSendStatus (Connection con, int rowId) {
		CallableStatement stmt;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.update_send_status(?,?,?,?,?)}");
			stmt.setInt(1, rowId);
			stmt.registerOutParameter(2, Types.INTEGER);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}