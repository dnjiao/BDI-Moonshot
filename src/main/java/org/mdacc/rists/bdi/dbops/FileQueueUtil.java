package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.OracleTypes;

import org.mdacc.rists.bdi.transfer.TransferUtils;

public class FileQueueUtil {
	public static ResultSet getUnsent (Connection con, String type) {
		CallableStatement stmt;
		ResultSet rs = null;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.get_unsent_file_by_type(?,?)}");
			stmt.setString(1, TransferUtils.convertTypeStr(type));
			stmt.registerOutParameter(2, OracleTypes.CURSOR);
			stmt.executeUpdate();
			
			// get cursor and cast it to ResultSet
			rs = (ResultSet) stmt.getObject(2);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return rs;
	}
	
	public static void updateSendStatus (Connection con, int rowId) {
		CallableStatement stmt;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.update_send_status(?)}");
			stmt.setInt(1, rowId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}