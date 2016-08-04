package org.mdacc.rists.bdi.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mdacc.rists.bdi.WorkflowUtils;

import oracle.jdbc.OracleTypes;

public class FileLocationUtil {
	
	final static DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("MMddyyyyHHmmss");
	
	public static void main (String[] args) {
		Connection con = DBConnection.getConnection();
		String type = "immunopath";
		String src = "/rsrch1/immunology/platform/immunotherapy_platform/IHC Immunoprofiling Data";
		String src2 = "/rsrch1/rists/moonshot/data/Apollo";
		System.out.println(getLastTimeStamp(con, type, src, "SRC"));

//		String path = "/rsrch1/ipct/krshaw_project/FIRE_Dropbox/Moonshots /rsrch1/rists/moonshot/data/stg";
//		DateTime dt = null;
//		dt = getLastTimeStamp(con, type, path);
//		System.out.println(dt);
//		List<String> srcList = getSourcesByType(con, type);
//		for (String src : srcList) {
//			System.out.println(src);
//		}
		
	}
	
	/**
	 * call stored procedure to insert/update timestamp of last pull in FILE_LOCATION_TB
	 * @param con - DB connection
	 * @param type - data type
	 * @param path - source directory
	 * @param dt - timestamp of last pull
	 */
	public static void setLastTimeStamp (Connection con, String filetype, String path, DateTime dt) {
		String typeStr = WorkflowUtils.convertTypeStr(filetype);
		String dtStr = FORMATTER.print(dt);
		try {
			CallableStatement stmt = con.prepareCall("{call FILE_LOCATION_UTIL.upsert_last_copy_ts(?,?,'SRC',?,?,?,?,?)}");
			System.out.println("Calling procedure FILE_LOCATION_UTIL.upsert_last_copy_ts: " + path);
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
			//System.out.println("Return code: " + ret);
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static DateTime getLastTimeStamp (Connection con, String filetype, String path, String srctype) {
		String typeStr = WorkflowUtils.convertTypeStr(filetype);
		String dtStr;
		DateTime dt = null;
		
		try {
			CallableStatement stmt = con.prepareCall("{call FILE_LOCATION_UTIL.get_last_copy_ts(?,?,?,?,?,?,?)}");
			stmt.setString(1, typeStr);
			stmt.setString(2, path);
			stmt.setString(3, srctype);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.executeUpdate();
			
			dtStr = stmt.getString(4);
			System.out.println("Last timestamp for " + filetype + " from " + path + ": " + dtStr);
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
	
	public static List<String> getSourcesByType(Connection con, String type) {
		CallableStatement stmt;
		ResultSet rs = null;
		List<String> srcList = null;
		
		try {
			System.out.println("Calling procedure FILE_LOCATION_UTIL.get_by_type for type " + type);
			stmt = con.prepareCall("{call FILE_LOCATION_UTIL.get_by_type(?,?,?,?,?,?)}");
			stmt.setString(1, WorkflowUtils.convertTypeStr(type));
			stmt.setString(2, "SRC_ROOT");
			stmt.registerOutParameter(3, OracleTypes.CURSOR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.registerOutParameter(5, Types.VARCHAR);
			stmt.registerOutParameter(6, Types.VARCHAR);
			stmt.executeUpdate();
			
			
			// get cursor and cast it to ResultSet
			rs = (ResultSet) stmt.getObject(3);
			srcList = ResultSetToList(rs);
			rs.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return srcList;
	}
	
	private static List<String> ResultSetToList(ResultSet rs) {
		List<String> list = new ArrayList<String>();
		try {	
			while (rs.next()) {
				String dir = rs.getString("DIR_PATH");
				list.add(dir);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}