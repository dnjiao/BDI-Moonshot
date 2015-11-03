package org.mdacc.rists.bdi.dbops;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection
{
	public static void main(String[] args) {
		Connection con = getConnection();
	}
	
	public static Connection getConnection() 
	{
		// TEST DB
		String connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
		String user = "ristore_owner_stg";
		String pass = "ristore987s";
		
		// Production DB
//		String connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risstg3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
//		String user = "ristore_owner";
//		String pass = "ristore4pgodzilla";

		Connection connection = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			connection = DriverManager.getConnection(connectionURL, user, pass);
			System.out.println("Connected to database.");
			
			
		} catch (SQLException e) {
			System.out.println("Connection failed.");
			System.exit(1);
		}  catch (ClassNotFoundException e) {
			System.out.println("Cannot find Oracle JDBC Driver.");
			e.printStackTrace();
			System.exit(1);
		} 
		return connection;
	}
}
