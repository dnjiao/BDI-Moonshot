package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Test;

public class DaoTest {
	@Test
	public void testInsert () {
		Connection conn = OracleDB.getConnection();
		try {
			CallableStatement pstmt = conn.prepareCall("{? = call FILE_TRANSFER.insert_file_audit_record(?,?,?,?)}");
			pstmt.registerOutParameter(1, Types.INTEGER);
			pstmt.setString(2, "export/data/flowcyto/1.csv");
			pstmt.setString(3, "/rsrch1/rists/moonshot/flowcyto/1.csv");
			pstmt.setString(4, "Protocol_Test");
			pstmt.setString(5, "Y");
			pstmt.executeUpdate();
			int ret = pstmt.getInt(1);
			System.out.println("Return code: " + Integer.toString(ret));
			pstmt.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}