package org.mdacc.rists.bdi.dbops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class FileTransferAuditUtil {
	public static int insertRecord (Connection con, String sourceUri, String destUri, String protocol) {
		int ret = 0;
		try {
			CallableStatement pstmt = con.prepareCall("{call FILE_TRANSFER_AUDIT_UTIL.insert_record(?,?,?,?,?,?,?)}");
			pstmt.setString(1, sourceUri);
			pstmt.setString(2, destUri);
			pstmt.setString(3, protocol.toUpperCase());
			pstmt.registerOutParameter(4, Types.INTEGER);
			pstmt.registerOutParameter(5, Types.VARCHAR);
			pstmt.registerOutParameter(6, Types.VARCHAR);
			pstmt.registerOutParameter(7, Types.VARCHAR);
			pstmt.executeUpdate();
			ret = pstmt.getInt(4);
			if (ret == 0) {
				System.out.println(pstmt.getString(6));
			}
			System.out.println("Return code: " + ret);
			pstmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return ret;
	}
	
	public static void updateFileQueueId (Connection con, List<String> fileList, int fileQueueId) {
		String[] fileArray = fileList.toArray(new String[fileList.size()]);
	}
	
}