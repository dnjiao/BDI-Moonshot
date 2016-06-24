package org.mdacc.rists.bdi.db.utils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class FileLoadUtil {
	
	public static void main(String[] args) throws SQLException {
		
		Connection con = DBConnection.getConnection();
		int num = getFileSeqNum(con, "/rsrch1/rists/moonshot/data/dev/foundation/xml/TRF051943_06212016121949.xml");
		BigDecimal bNum = new BigDecimal(num);
		System.out.println(bNum);
		con.close();
	}
	
	public static int getFileSeqNum(Connection con, String fileUri) {
		CallableStatement stmt;
		int seqNum = 1;
		try {
			System.out.println("Calling procedure FILE_LOAD_UTIL.get_file_seq_num for " + fileUri);
			stmt = con.prepareCall("{call FILE_LOAD_UTIL.get_file_seq_num(?,?,?,?,?)}");
			stmt.setString(1, fileUri);
			stmt.registerOutParameter(2, Types.INTEGER);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.executeUpdate();
			seqNum = stmt.getInt(2);
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return seqNum;
	}
}
