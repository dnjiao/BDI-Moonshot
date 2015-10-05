package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.Test;
import org.mdacc.rists.bdi.dbops.DBConnection;

public class DaoTest {
	@Test
	public void testInsert () {
		Connection conn = DBConnection.getConnection();
		try {
			CallableStatement pstmt = conn.prepareCall("{? = call FILE_TRANSFER.insert_file_queue_record(?,?,?,?,?,?,?,?)}");
			pstmt.registerOutParameter(1, Types.INTEGER);
			pstmt.setString(2, "export/data/flowcyto/1.csv");
			pstmt.setString(3, "/rsrch1/rists/moonshot/flowcyto/1.csv");
			pstmt.setString(4, "Protocol_Test");
			pstmt.setString(5, "P");
			pstmt.registerOutParameter(6, Types.INTEGER);
			pstmt.registerOutParameter(7, Types.VARCHAR);
			pstmt.registerOutParameter(8, Types.VARCHAR);
			pstmt.registerOutParameter(9, Types.VARCHAR);
			pstmt.executeUpdate();
			int ret = pstmt.getInt(1);
			if (ret == 0) {
				System.out.println(pstmt.getString(8));
			}
			System.out.println("Return code: " + Integer.toString(ret));
			pstmt.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
//	@Test
//	public void testLoader() {
//		Connection conn = OracleDB.getConnection();
//		try {
//			CallableStatement pstmt = conn.prepareCall("{call FILE_LOADER.process_file(?,?,?,?,?,?)}");
//			pstmt.setString(1, null);
//			pstmt.setString(2, null);
//			pstmt.setString(3, null);
//			pstmt.setString(4, "Y");
//			pstmt.setString(5, null);
//			pstmt.setString(6, null);
//			pstmt.executeUpdate();
//			
////			System.out.println("Return code: " + Integer.toString(ret));
//			pstmt.close();
//			conn.close();
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
//	}
}