package jdbc;

import java.sql.Connection;

import Utils.JDBCUtils;


public class Main {
	public static void main(String[] args)
	{
		try (Connection connection = JDBCUtils.getNewConnection())
		{
			JDBCUtils.printArticles(connection);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}

}
