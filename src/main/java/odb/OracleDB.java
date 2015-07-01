package odb;

import java.sql.Connection;
import java.sql.DriverManager;

public class OracleDB
{
	public Connection getConnection(String user, String pass)
	{
		try
		{
			String connectionURL = "ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
			Connection connection = null;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(connectionURL, user, pass);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return connection;
	}
}
