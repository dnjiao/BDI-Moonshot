package org.mdacc.rists.bdi.dbops;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class FileProcess {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Invalid arguments. Usage: FileProcess [type]");
			System.exit(1);
		}
		final String TYPE = args[0].toLowerCase();
		loader(TYPE);
	}
	public static void loader (String type) {
		Connection conn = DBConnection.getConnection();
		String[] arr = new String[6];
		if (type.equals("vcf"))
			arr[0] = "Y";
		if (type.equals("cnv"))
			arr[1] = "Y";
		if (type.equals("gene"))
			arr[2] = "Y";
		if (type.equals("exon"))
			arr[2] = "Y";
		if (type.equals("junctions"))
			arr[2] = "Y";
		if (type.equals("flowcyto"))
			arr[3] = "Y";
		if (type.equals("immunopath"))
			arr[4] = "Y";
		if (type.equals("mapping"))
			arr[5] = "Y";
		try {
			CallableStatement stmt = conn.prepareCall("{call FILE_PROCESS.load_file(?,?,?,?,?,?,?,?,?)}");
			for (int i = 1; i < 7; i ++) {
				stmt.setString(i, arr[i - 1]);
			}
			stmt.registerOutParameter(7, Types.VARCHAR);
			stmt.registerOutParameter(8, Types.VARCHAR);
			stmt.registerOutParameter(9, Types.VARCHAR);
			stmt.executeUpdate();
			System.out.println("Calling procedure FILE_PROCESS.load_file.");
//			System.out.println("Error code: " + stmt.getString(7));
//			System.out.println("Error description: " + stmt.getString(8));
//			System.out.println("Error trace: " + stmt.getString(9));
			stmt.close();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}