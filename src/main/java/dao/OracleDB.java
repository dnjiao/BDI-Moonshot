package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDB
{
    public static void main(String[] args) {
        try 
        {
    		Connection con = getConnection();
        	con.close();
        }
        
        catch (SQLException e) {
        	e.printStackTrace();
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
	public static Connection getConnection() throws Exception
	{
		
		String connectionURL = "jdbc:oracle:thin:@ldap://mdaoid.mdanderson.org:389/risdev3, cn=OracleContext,dc=mdacc,dc=tmc,dc=edu";
		Connection connection = null;
		String user = "BDI_USER";
		String pass = "bdiuser123";
		Class.forName("oracle.jdbc.OracleDriver").newInstance();
		connection = DriverManager.getConnection(connectionURL, user, pass);
		System.out.println("Connected to database");
		return connection;
	}
}
