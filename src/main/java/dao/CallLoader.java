package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class CallLoader {
	public static void main(String[] args) {
		final String TYPE = System.getenv("TYPE").toLowerCase();
		loader(TYPE);
	}
	public static void loader (String type) {
		Connection conn = OracleDB.getConnection();
		String[] arr = new String[6];
		if (type.equals("vcf"))
			arr[0] = "Y";
		if (type.equals("cnv"))
			arr[1] = "Y";
		if (type.equals("rna"))
			arr[2] = "Y";
		if (type.equals("flowcyto"))
			arr[3] = "Y";
		if (type.equals("immunopath"))
			arr[4] = "Y";
		if (type.equals("mapping"))
			arr[5] = "Y";
		try {
			CallableStatement pstmt = conn.prepareCall("{call FILE_LOADER.process_file(?,?,?,?,?,?)}");
			for (int i = 1; i < 7; i ++) {
				pstmt.setString(i, arr[i - 1]);
			}
			pstmt.executeUpdate();
			
			pstmt.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}