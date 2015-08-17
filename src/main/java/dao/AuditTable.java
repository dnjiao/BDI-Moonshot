package dao;

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

public class AuditTable {
	public static int insertSingle (Connection con, String sourceUri, String destUri, String protocol) {
		int ret = 0;
		try {
			
			CallableStatement pstmt = con.prepareCall("{? = call FILE_TRANSFER.insert_file_queue_record(?,?,?,?,?,?,?,?)}");
			pstmt.registerOutParameter(1, Types.INTEGER);
			pstmt.setString(2, sourceUri);
			pstmt.setString(3, destUri);
			pstmt.setString(4, protocol.toUpperCase());
			pstmt.setString(5, "P");
			pstmt.registerOutParameter(6, Types.INTEGER);
			pstmt.registerOutParameter(7, Types.VARCHAR);
			pstmt.registerOutParameter(8, Types.VARCHAR);
			pstmt.registerOutParameter(9, Types.VARCHAR);
			pstmt.executeUpdate();
			ret = pstmt.getInt(1);
			if (ret == 0) {
				System.out.println(pstmt.getString(8));
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
	
	public static File insertMulti (Connection con, File insertLog, String protocol) {
		File failed = new File(insertLog.getParent(), "tmp_insert.log");
		PrintWriter writer = null;
		BufferedReader br = null;
		try {
			writer=new PrintWriter(failed);
			br = new BufferedReader(new FileReader(insertLog));
			String line, linestr, source, dest;
			while ((line = br.readLine()) != null) {
				linestr = line.replaceAll("(\\r|\\n)", "");
				source = linestr.split("\t")[0];
				dest = linestr.split("\t")[1];
				if (insertSingle(con, source, dest, protocol) == 0) {
					writer.println(linestr);
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
		return failed;
		
	}
}