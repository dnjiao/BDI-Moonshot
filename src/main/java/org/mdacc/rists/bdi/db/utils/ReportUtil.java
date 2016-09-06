package org.mdacc.rists.bdi.db.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import oracle.jdbc.OracleTypes;

public class ReportUtil {
	
	public static void main(String[] args) throws SQLException, IOException {
		Connection con = DBConnection.getConnection();
//		generateReport(con);
		System.out.println(getLastReportTime(con));
		String reportDir = "/Users/djiao/Work/moonshot/reports";
		createCSV(con, reportDir);
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
	
	private static String getLastReportTime (Connection con) {
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
	
	/**
	 * Generate report in csv format 
	 * @param con - db connection instance
	 * @param path - path to report folder on GPFS
	 * @return - full path of csv file
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static String createCSV(Connection con, String path) throws SQLException, IOException {
		DateTime now = new DateTime();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
		String filepath = path + "/ristore_report_" + formatter.print(now) + ".csv";
		try {
			Files.createFile(Paths.get(filepath));
		} catch (FileAlreadyExistsException ignored) {
		}
		File csv = new File(filepath);
		PrintWriter writer = new PrintWriter(csv);
		
		// query report tb and get results for last report date
		String day = truncateDate(getLastReportTime(con));
		String query = "select \"Data Domain\", PI, \"Protocol Count(RIStore)\", \"Protocol Count(TRA)\", \"Protocol Count(NonProtocol)\", "
				+ "\"Sample Count(RIStore)\", \"Sample Count(TRA)\", \"File Count(RIStore)\", \"File Count(Loaded)\", \"File Count(TRA)\", "
				+ "\"Report Date\", \"From Date\", \"To Date\" "
				+ "from report_pi_tb where TRUNC(\"Report Date\") = TO_DATE(?, 'MMDDYYYY') order by row_id asc";
		System.out.println(query);
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setString(1, day);
		ResultSet rs = stmt.executeQuery();
		
		ResultSetMetaData rsmd = rs.getMetaData();
	    int numOfCols = rsmd.getColumnCount();
	    //print col names
	    String row = rsmd.getColumnName(1);
	    for (int i = 2; i <= numOfCols; i++) {
	    	row += "," + rsmd.getColumnName(i);
	    }
	    writer.println(row);
	    //print col values
	    while (rs.next()) {
	        for (int i = 1; i <= numOfCols; i++) {
	        	if (i == 1) {
	        		row = rs.getString(1);
	        	} else {
	        		String val = rs.getString(i);
	        		if (val != null) {
	        			if (rsmd.getColumnClassName(i).equalsIgnoreCase("java.sql.Timestamp")) {
		        			val = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(rs.getTimestamp(i));
	        			}
	        			row += "," + val;
	        		} else {
	        			row += ",";
	        		}
	        	}
	        }
	        writer.println(row);
	    }
		writer.close();
		return filepath;
	}
	
	/**
	 * Truncate timestamp and extract only the day
	 * @param datetime - timestamp
	 * @return - string of day
	 */
	private static String truncateDate(String datetime) {
		return datetime.substring(0, 8);
	}
}
