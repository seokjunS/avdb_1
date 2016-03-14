import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.mariadb.jdbc.Driver;

public class DataManager {
	private String host, username, password, driver;
	
	public DataManager() {
		this.host = "jdbc:mariadb://147.46.15.238:3306/ADB-2016-21211?allowMultiQueries=true";
		this.username = "ADB-2016-21211";
		this.password = "ADB-2016-21211";
//		this.driver = "com.mariadb.jdbc.Driver";
	}
	
    public Connection createConnection() throws ClassNotFoundException, SQLException {
    	 
        Connection connection;
 
//        System.out.println("host: " + this.host + " username: " + 
//        		this.username + " password: " + this.password + " ndriver: " + this.driver);
 
//        Class.forName(driver);
//        System.out.println("--------------------------");
//        System.out.println("DRIVER: " + driver);
        connection = DriverManager.getConnection(host, username, password);
//        System.out.println("CONNECTION: " + connection);
 
        return connection;
    }
    
    public void createInversedIndexTable() {
    	try {
    		String query = "DROP TABLE IF EXISTS inverted_index; " +
    				"CREATE TABLE inverted_index ( " +
    				"term varchar(1000) NOT NULL, " +
    				"id int(11) NOT NULL " +
    				") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;";
 
    		Statement statement = createConnection().createStatement();
    		int rs = statement.executeUpdate(query);
    		System.out.println("create inverted index table: " + rs);
    		statement.close();
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }
 
    public void makeInversedIndex() {
        try {
            Statement statement = createConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM wiki limit 10;");
            
            while (rs.next()) {
            	String query = "INSERT INTO inverted_index VALUES (?, ?)";
            	PreparedStatement ps = createConnection().prepareStatement(query);
            	
            	String id = rs.getString("id");
            	for(String res : TokenizerDemo.tokenize(rs.getString("text")) ) {
            		ps.setString(1, res);
            		ps.setString(2, id);
            		ps.addBatch();
            		ps.clearParameters();
//            		System.out.println(res);
            	}
            	
            	int[] rs2 = ps.executeBatch();
            	int batchRes = 1;
            	for (int r : rs2) {
            		batchRes *= r;
            	}
            	System.out.println("Insert result: " + batchRes);
            	
            }
            statement.close();
            
            
        } catch (Exception ex) {
        	System.out.println(ex);
        }
    }
}
