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
 
        connection = DriverManager.getConnection(host, username, password);
 
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
            ResultSet rs = statement.executeQuery("SELECT * FROM wiki;");
            
            String query = "INSERT INTO inverted_index VALUES (?, ?)";
            
            int batchLimit = 100000;
            int batchCounter = 0;
            int batchRes = 1;
        	PreparedStatement ps = createConnection().prepareStatement(query);
        	
            
            while (rs.next()) {
            	String id = rs.getString("id");
            	for(String res : TokenizerDemo.tokenize(rs.getString("text")) ) {
            		ps.setString(1, res);
            		ps.setString(2, id);
            		ps.addBatch();
            		ps.clearParameters();
            		batchCounter++;
            		
            		if (batchCounter >= batchLimit) {
            			int[] tmpRes = ps.executeBatch();
            			for (int r : tmpRes) {
            				batchRes *= r;
            			}
            			System.out.println("Batch Insert(" + tmpRes.length + ") result: " + batchRes);
            			ps.close();
            			
            			ps = createConnection().prepareStatement(query);
            			batchCounter = 0;
            		}
            	}
            		
            }
            
            int[] tmpRes = ps.executeBatch();
			for (int r : tmpRes) {
				batchRes *= r;
			}
			System.out.println("Batch Insert(" + tmpRes.length + ") result: " + batchRes);
			ps.close();
			
			ps = createConnection().prepareStatement(query);
            
            statement.close();
            
            
        } catch (Exception ex) {
        	System.out.println(ex);
        }
    }
    
    public void setIndexToInvertedIndex() {
    	try {
    		String query = "CREATE INDEX II_TERM_INDEX ON inverted_index(term); " +
    				"CREATE INDEX II_ID_INDEX ON inverted_index(id); " +
    				"CREATE INDEX II_TERM_ID_INDEX ON inverted_index(term, id); ";
 
    		Statement statement = createConnection().createStatement();
    		int rs = statement.executeUpdate(query);
    		System.out.println("create index on inverted index table: " + rs);
    		statement.close();
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    public void createTFIDFTable() {
    	try {
    		String query = "DROP TABLE IF EXISTS tf_idf; " +
    				"CREATE TABLE tf_idf ( " +
    				"term varchar(1000) NOT NULL, " +
    				"id int(11) NOT NULL, " +
    				"score DOUBLE(21,20) NOT NULL " +
    				") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;";
 
    		Statement statement = createConnection().createStatement();
    		int rs = statement.executeUpdate(query);
    		System.out.println("create TF-IDF table: " + rs);
    		statement.close();
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    public void calcTFIDF() {
    	try {
    		String query = "CREATE INDEX II_TERM_INDEX ON inverted_index(term); " +
    				"CREATE INDEX II_ID_INDEX ON inverted_index(id); ";
 
    		Statement statement = createConnection().createStatement();
    		int rs = statement.executeUpdate(query);
    		System.out.println("create index on inverted index table: " + rs);
    		statement.close();
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }

}
