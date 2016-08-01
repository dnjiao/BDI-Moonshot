package org.mdacc.rists.bdi.db.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import oracle.jdbc.OracleTypes;

public class ReportUtil {
	
	public static void main(String[] args) {
		Connection con = DBConnection.getConnection();
		generateReport(con);
	}
	public static void generateReport (Connection con) {
		try {
			// full report
			CallableStatement stmt = con.prepareCall("{call REPORT_UTIL.generate_report_pi(?,?,?)}");
			stmt.setString(1, "01011900");
			stmt.setString(2, "01019999");
			stmt.registerOutParameter(3, OracleTypes.CURSOR);
			stmt.executeUpdate();
			stmt.close();
			
			// daily report
			stmt = con.prepareCall("{call REPORT_UTIL.generate_report_pi(?,?,?)}");
			DateTimeFormatter dayFormat = DateTimeFormat.forPattern("MMddyyyy");
			DateTimeFormatter timeFormat = DateTimeFormat.forPattern("MMddyyyyHHmmss");
			DateTime now = new DateTime();
			String start = dayFormat.print(now) + "050000";
			String end = timeFormat.print(now);
			stmt.setString(1, start);
			stmt.setString(2, end);
			stmt.registerOutParameter(3, OracleTypes.CURSOR);
			stmt.executeUpdate();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		} 
	}
}
