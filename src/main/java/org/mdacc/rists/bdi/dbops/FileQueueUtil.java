package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import oracle.jdbc.OracleTypes;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.rists.bdi.transfer.TransferUtils;

public class FileQueueUtil {
	
	public static void main(String[] args) {
		DateTime dt = new DateTime();
		Connection con = DBConnection.getConnection();
		getUnsent(con, "exon");
		
	}
	public static ResultSet getUnsent (Connection con, String type) {
		CallableStatement stmt;
		ResultSet rs = null;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.get_unsent_file_by_type(?,?,?,?,?)}");
//			String t = TransferUtils.convertTypeStr(type);
			stmt.setString(1, TransferUtils.convertTypeStr(type));
			stmt.registerOutParameter(2, OracleTypes.CURSOR);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.executeUpdate();
			System.out.println("Calling procedure FILE_QUEUE_UTIL.get_unsent_file_by_type for type " + type);
			
			// get cursor and cast it to ResultSet
			rs = (ResultSet) stmt.getObject(2);
			
//			//DEBUG: print out ResultSet content.
//			ResultSetMetaData rsmd = rs.getMetaData();
//		    int columnsNumber = rsmd.getColumnCount();
//		    while (rs.next()) {
//		        for (int i = 1; i <= columnsNumber; i++) {
//		            if (i > 1) System.out.print(",  ");
//		            String columnValue = rs.getString(i);
//		            System.out.print(columnValue + " " + rsmd.getColumnName(i));
//		        }
//		        System.out.println("");
//		    }
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return rs;
	}
	
	public static int insertRecord (Connection con, String filepath, String type) {
		CallableStatement stmt;
		int queueId = 0;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.insert_record(?,?,?,?,?,?,?)}");
			stmt.setString(1, filepath);
			if (type.equals("vcf") || type.equals("cnv") || type.equals("gene") || type.equals("exon") || type.equals("junction")) {
				stmt.setString(2, null);
			}
			else {
				stmt.setString(2, "P");
			}
			stmt.registerOutParameter(3, Types.INTEGER);
			stmt.registerOutParameter(4, Types.INTEGER);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.executeUpdate();
			System.out.println("Calling procedure FILE_QUEUE_UTIL.insert_record for type " + type);
			queueId = stmt.getInt(4);
			// if update/insert is not success, print out error description
			if (queueId == 0) {
				System.out.println("Error description: " + stmt.getString(6));
				System.out.println("Error trace: " + stmt.getString(7));
			}
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return queueId;
	}
	
	public static void updateSendStatus (Connection con, int rowId, DateTime dt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("MMddyyyyHHmmss");
		String dtStr = formatter.print(dt);
		CallableStatement stmt;
		try {
			stmt = con.prepareCall("{call FILE_QUEUE_UTIL.update_send_status(?,?,?,?,?,?)}");
			stmt.setInt(1, rowId);
			stmt.setString(2, dtStr);
			stmt.registerOutParameter(3, Types.INTEGER);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.executeUpdate();
			System.out.println("Calling procedure FILE_QUEUE_UTIL.update_send_status.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}