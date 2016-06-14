package org.mdacc.rists.bdi.db.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection
{
	// number of retries
	final static int MAXRETRY = 10;
	// pause time between two retries (ms)
	final static int PAUSETIME = 1000;
	
	public static void main(String[] args) {
		Connection con = getConnection();
	}
	
	public static Connection getConnection() 
	{
		String connectionURL = null;
		String user = null;
		String pass = null;
		String env = System.getenv("DEV_ENV");
		
		// DEV DB
		if (env.equalsIgnoreCase("dev")) {
			connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
			user = "ristore_owner";
			pass = "ristoreowner987";
		}
		// QA DB
		else if (env.equalsIgnoreCase("qa")) {
			connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
			user = "ristore_owner_qa";
			pass = "ristore987q";
		}
		// Staging DB
		else if (env.equalsIgnoreCase("stg")) {
			connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
			user = "ristore_owner_stg";
			pass = "ristore987s";
		}
		// Production DB
		else if (env.equalsIgnoreCase("prod")) {
			connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risstg3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
			user = "ristore_owner";
			pass = "ristore4pgodzilla";
		}
		else {
			System.err.println("Invalid dev environment.");
			System.exit(1);
		}
		
		
		Connection connection = null;
		int retries = 1;
		while (connection == null && retries <= MAXRETRY) {
			try {
				Class.forName("oracle.jdbc.OracleDriver");
				connection = DriverManager.getConnection(connectionURL, user, pass);
				retries ++;
				Thread.sleep(PAUSETIME);
			} catch (SQLException e) {
				System.out.println("Connection failed, retry #" + retries);
			}  catch (ClassNotFoundException e) {
				System.err.println("Cannot find Oracle JDBC Driver.");
				System.exit(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		if (connection != null) {
			System.out.println("Connected to " + env + " database.");
		}
		else {
			System.err.println("Max retry times reached. Connection to " + env + " database failed.");
			System.exit(1);
		}
		return connection;
	}
}
