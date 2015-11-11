package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.rists.bdi.transfer.TransferUtils;

public class FileLocationUtil {
	
	final static DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	
	public static void main (String[] args) {
		String type = "junction";
		String path = "/rsrch1/ipct/krshaw_project/FIRE_Dropbox/Moonshots /rsrch1/rists/moonshot/data/stg";
		DateTime dt = null;
		Connection con = DBConnection.getConnection();
		dt = getLastTimeStamp(con, type, path);
		System.out.println(dt);
		
	}
	
	/**
	 * call stored procedure to insert/update timestamp of last pull in FILE_LOCATION_TB
	 * @param con - DB connection
	 * @param type - data type
	 * @param path - source directory
	 * @param dt - timestamp of last pull
	 */
	public static void setLastTimeStamp (Connection con, String type, String path, DateTime dt) {
		String typeStr = TransferUtils.convertTypeStr(type);
		String dtStr = FORMATTER.print(dt);
		try {
			CallableStatement stmt = con.prepareCall("{call FILE_LOCATION_UTIL.upsert_last_copy_ts(?,?,'SRC',?,?,?,?,?)}");
			
			stmt.setString(1, typeStr);
			stmt.setString(2, path);
			stmt.setString(3, dtStr);
			stmt.registerOutParameter(4, Types.INTEGER);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.executeUpdate();
			
			int ret = stmt.getInt(4);
			// if update/insert is not success, print out error description
			if (ret == 0) {
				System.out.println("Error description: " + stmt.getString(6));
				System.out.println("Error trace: " + stmt.getString(7));
			}
			System.out.println("Return code: " + ret);
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static DateTime getLastTimeStamp (Connection con, String type, String path) {
		String typeStr = TransferUtils.convertTypeStr(type);
		String dtStr;
		DateTime dt = null;
		
		try {
			CallableStatement stmt = con.prepareCall("{call FILE_LOCATION_UTIL.get_last_copy_ts(?,?,?,?,?,?)}");
			stmt.setString(1, typeStr);
			stmt.setString(2, path);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.executeUpdate();
			dtStr = stmt.getString(3);
			if (dtStr != null) {
				dt = FORMATTER.parseDateTime(dtStr);
			}
			stmt.close();
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return dt;
	}
}