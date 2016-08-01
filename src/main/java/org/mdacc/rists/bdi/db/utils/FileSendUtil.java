package org.mdacc.rists.bdi.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class FileSendUtil {
	public static void main(String[] args) {
		Connection con = DBConnection.getConnection();
		String path = "/rsrch1/rists/moonshot/data/dev/foundation/xml/TRF006602_1461592138918_06212016121949.xml";
		String sta = "S";
		insertRecord(con, path, sta, 85662, 10, 2);
	}

	public static void insertRecord(Connection con, String status, String filePath, int fqId, int typeId, int consumerId) {
		CallableStatement stmt;
		try {
			System.out.println("Calling procedure FILE_SEND_UTIL.insert_record for " + filePath);
			stmt = con.prepareCall("{call FILE_SEND_UTIL.insert_record(?,?,?,?,?,?,?,?,?)}");
			stmt.setString(1, filePath);
			stmt.setString(2, status);
			stmt.setInt(3, fqId);
			stmt.setInt(4, typeId);
			stmt.setInt(5, consumerId);
			stmt.registerOutParameter(6, Types.DECIMAL);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.registerOutParameter(8, Types.VARCHAR);
			stmt.registerOutParameter(9, Types.VARCHAR);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
}
