package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class Loaders {
	public static void callLoader (String type) {
		Connection conn = OracleDB.getConnection();
		String[] arr = new String[6];
		if (type.equalsIgnoreCase("vcf"))
			arr[0] = "Y";
		if (type.equalsIgnoreCase("cnv"))
			arr[1] = "Y";
		if (type.equalsIgnoreCase("rna"))
			arr[2] = "Y";
		if (type.equalsIgnoreCase("flowcyto"))
			arr[3] = "Y";
		if (type.equalsIgnoreCase("immunopath"))
			arr[4] = "Y";
		if (type.equalsIgnoreCase("mapping"))
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