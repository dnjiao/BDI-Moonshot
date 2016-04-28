package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class FileChecksumUtil {
	public static void main(String[] args) {
		ValidateChecksum("/rsrch1/rists/moonshot/data/dev/foundation/xml/TRF092336_1460044637142.xml", 
				"c8ea3cff5f9085e8705bbebca6374ced", "FM");
	}
	
	public static int ValidateChecksum(String filepath, String checksum, String type) {
		int returnInt = 0;
		Connection conn = DBConnection.getConnection();
		try {
			CallableStatement stmt = conn.prepareCall("{call FILE_CHECKSUM_UTIL.valid_file_by_source(?,?,?,?,?,?,?)}");
			stmt.setString(1,  filepath);
			stmt.setString(2, checksum);
			stmt.setString(3, type);

			stmt.registerOutParameter(4, Types.INTEGER);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.executeUpdate();
			System.out.println("Calling procedure FILE_CHECKSUM_UTIL.valid_file_by_source.");
//				System.out.println("Error code: " + stmt.getString(7));
//				System.out.println("Error description: " + stmt.getString(8));
//				System.out.println("Error trace: " + stmt.getString(9));
			returnInt = stmt.getInt(4);
			stmt.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return returnInt;
	}
}
