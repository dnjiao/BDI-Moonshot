package odb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDB
{
	public Connection getConnection() throws SQLException
	{
		
		String connectionURL = "ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
		Connection connection = null;
		String user = "BDI_USER";
		String pass = "dbiuser123";
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		connection = DriverManager.getConnection(connectionURL, user, pass);
		System.out.println("Connected to database");
		return connection;
	}
}
