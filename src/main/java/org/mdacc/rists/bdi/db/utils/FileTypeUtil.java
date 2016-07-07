package org.mdacc.rists.bdi.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class FileTypeUtil {
	public static void main(String[] args) {
		Connection con = DBConnection.getConnection();
		System.out.println(getFileTypeId(con, "FM"));
	}

	public static int getFileTypeId(Connection con, String type) {
		CallableStatement stmt;
		int id = -1;
		try {
			System.out.println("Calling procedure FILE_TYPE_UTIL.get_file_type_id for " + type);
			stmt = con.prepareCall("{call FILE_TYPE_UTIL.get_file_type_id(?,?,?,?,?)}");
			stmt.setString(1, type);
			stmt.registerOutParameter(2, Types.DECIMAL);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.executeUpdate();
			id = stmt.getInt(2);
			stmt.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
}
