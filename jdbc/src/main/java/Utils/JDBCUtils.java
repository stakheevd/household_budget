package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtils {
	public static Connection getNewConnection() throws SQLException
	{
		String dbURL = "jdbc:postgresql://localhost:5432/household_budget";
		Connection connection = DriverManager.getConnection(dbURL);
		
		if (connection.isValid(1))
		{
			System.out.println("Connection successful!!!");
		}
		
		return connection;
	}
	
	public static void printArticles (Connection connection) throws SQLException
	{
		try (Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT * FROM articles"))
		{
			while (rs.next())
			{
				int id = rs.getInt("id");
				String name = rs.getString("name");
				System.out.println(id + " : " + name);
			}
		}
	}
}
