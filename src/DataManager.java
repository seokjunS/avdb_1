import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mariadb.jdbc.Driver;

public class DataManager {
	private String host, username, password, driver;
	
	public DataManager() {
		this.host = "jdbc:mariadb://147.46.15.238:3306";
		this.username = "ADB-2016-21211";
		this.password = "ADB-2016-21211";
		this.driver = "com.mariadb.jdbc.Driver";
	}
	
    public Connection createConnection() throws ClassNotFoundException, SQLException {
    	 
        Connection connection;
 
        System.out.println("host: " + this.host + " username: " + 
        		this.username + " password: " + this.password + " ndriver: " + this.driver);
 
//        Class.forName(driver);
        System.out.println("--------------------------");
        System.out.println("DRIVER: " + driver);
        connection = DriverManager.getConnection(host, username, password);
        System.out.println("CONNECTION: " + connection);
 
        return connection;
    }
 
    public void runSqlStatement() {
        try {
            Statement statement = createConnection().createStatement();
            ResultSet rs = statement.executeQuery("SHOW DATABASES;");
            
            while (rs.next()) {
            	System.out.println("result : " + rs.getString("Database") );
            	
            }
            
            
            
        } catch (Exception ex) {
        	System.out.println(ex);
        }
    }
}
