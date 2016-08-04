package org.mdacc.rists.bdi.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import oracle.jdbc.OracleTypes;

public class ReportUtil {
	
	public static void main(String[] args) {
		Connection con = DBConnection.getConnection();
		generateReport(con);
//		System.out.println(getLastReportTime(con));
	}
	public static void generateReport (Connection con) {
		try {
			// weekly report
			CallableStatement stmt = con.prepareCall("{call REPORT_UTIL.generate_report_pi(?,?,?)}");
			System.out.println("Generating weekly report.");
			DateTimeFormatter formatter = DateTimeFormat.forPattern("MMddyyyyHHmmss");
			DateTime today = new DateTime();
			String start = getLastReportTime(con);
			String end = formatter.print(today);
			stmt.setString(1, start);
			stmt.setString(2, end);
			stmt.registerOutParameter(3, OracleTypes.CURSOR);
			stmt.executeUpdate();
			
			// full report
			System.out.println("Generating full report.");
			stmt.setString(1, "01011900");
			stmt.setString(2, "01019999");
			stmt.registerOutParameter(3, OracleTypes.CURSOR);
			stmt.executeUpdate();
			stmt.close();
		
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		} 
	}
	
	public static String getLastReportTime (Connection con) {
		String timeStr = null;
		try {
			CallableStatement stmt = con.prepareCall("{call REPORT_UTIL.get_last_report_time(?,?,?,?)}");
			stmt.registerOutParameter(1, Types.VARCHAR);
			stmt.registerOutParameter(2, Types.VARCHAR);
			stmt.registerOutParameter(3, Types.VARCHAR);
			stmt.registerOutParameter(4, Types.VARCHAR);
			stmt.executeUpdate();
			timeStr = stmt.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return timeStr;
	}
}
