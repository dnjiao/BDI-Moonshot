package org.mdacc.rists.bdi;


import java.sql.Connection;

import org.mdacc.rists.bdi.db.utils.DBConnection;
import org.mdacc.rists.bdi.db.utils.ReportUtil;

public class RunReport {
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: RunReport [email_to] [dir_path]");
			System.exit(1);
		}
		Connection con = DBConnection.getConnection();
		ReportUtil.generateReport(con);
		String csvPath = ReportUtil.createCSV(con, args[1]);
		SendEmail.sendReportEmail(args[0], csvPath);
	}
}