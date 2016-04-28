package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class RunReport {
	
	public static void main(String[] args) {
		Connection con = DBConnection.getConnection();
		generateReport(con);
	}
	
	public static void generateReport (Connection con) {
		try {
			CallableStatement stmt = con.prepareCall("{call generate_report_pi()}");
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}