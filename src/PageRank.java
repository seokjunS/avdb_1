import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import Jama.Matrix;
import Jama.util.*;

public class PageRank {
	private String host, username, password, driver;
	private Integer[] entries;
	private Matrix adjacencyMat, pageMat, jumpMat;
	
	private double delta = 0.15;
	private double epsilon = 1.0 * Math.exp(-8.0);
	
	public PageRank(String host, String username, String password, String driver) {
		this.host = host;
		this.username = username;
		this.password = password;
//		this.driver = "com.mariadb.jdbc.Driver";
		
		this.entries = null;
		this.adjacencyMat = null;
		this.pageMat = null;
		init();
	}
	
	public void init() {
		// get adjacency Mat
		getAdjacencyMatrix();
		
		// init pagerank matrix and jump matrix
		int entrySize = this.entries.length;
		double[][] initPageMat = new double[1][entrySize];
		double[][] initJumpMat = new double[1][entrySize];
		for(int i = 0; i < entrySize; i++) { 
			initPageMat[0][i] = 1.0 / entrySize; 
			initJumpMat[0][i] = this.delta / entrySize;
		}
		this.pageMat = new Matrix( initPageMat );
		this.jumpMat = new Matrix( initJumpMat );
		
		// random suffer
		this.randomSuffer();
		this.updateToDB();
	}
	
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Connection connection;
        connection = DriverManager.getConnection(host, username, password);
        return connection;
    }
    
    public void getAdjacencyMatrix() {
    	try {
    		String query = "SELECT s1.id_from, s1.id_to, 1 / s2.c AS score " +
    				"FROM (SELECT id_from, id_to FROM link GROUP BY id_from, id_to) s1 " +
    				"LEFT JOIN (SELECT id_from, count(distinct id_to) AS c FROM link GROUP BY id_from) s2 " + 
    				"ON s1.id_from = s2.id_from; ";
    		
        	Statement statement = createConnection().createStatement();
            ResultSet rs = statement.executeQuery(query);
            
            HashSet<Integer> entrySet = new HashSet<Integer>();
            Hashtable<String, Double> links = new Hashtable<String, Double>();
            
            // get data
            while (rs.next()) {
            	String from = rs.getString("id_from");
            	String to = rs.getString("id_to");
            	String tmpKey = from + "," + to;

            	links.put(tmpKey, rs.getDouble("score"));
            	entrySet.add(new Integer(from));
            	entrySet.add(new Integer(to));
            }
            
            int entrySize = entrySet.size();
            Integer[] entries = entrySet.toArray( new Integer[entrySize] );
            Arrays.sort( entries );

            double[][] adjacency = new double[entrySize][entrySize];
            
            Integer from, to;
            String tmpKey;
            for(int i = 0; i < entrySize; i++) {
            	for (int j = 0; j < entrySize; j++) {
            		from = entries[ i ];
            		to = entries[ j ];
            		tmpKey = from + "," + to;
            		
            		Double link = links.get(tmpKey);
            		if ( link == null ) {
            			adjacency[i][j] = 0.0;
            		}
            		else {
            			adjacency[i][j] = link;
            		}
            	}
            }
            Matrix adjacencyMat = new Matrix(adjacency);
            
            this.entries = entries;
            this.adjacencyMat = adjacencyMat;
            System.out.println("Get Adjacency Matrix");
    	}
    	catch (Exception e) {
    		System.out.println("getAdjacencyMatrix: " + e);
    	}
    }
    
    public void randomSuffer() {
    	try {
    		while (true) {
    			Matrix tmp = this.pageMat.times(this.adjacencyMat).times(1 - this.delta);
        		Matrix processedMat = this.jumpMat.plus(tmp);
        		
        		double[][] res = processedMat.minus(this.pageMat).getArray();
        		double diff = 0.0;
        		
        		for (double item : res[0]) {
        			diff += Math.abs(item);
        		}
        		
        		System.out.println("Randon suffered => diff: " + diff);
        		this.pageMat = processedMat;
        		
        		if (diff < this.epsilon) {
        			System.out.println("Randon suffer done");
        			break;
        		}
    		}
    		
    	}
    	catch (Exception e) {
    		System.out.println("randomSuffer: " + e);
    	}
    }
    
    public void updateToDB() {
    	try {
    		// first create table
    		String query = "DROP TABLE IF EXISTS pagerank; " +
    				"CREATE TABLE pagerank ( " +
    				"id int(11) NOT NULL, " +
    				"score DOUBLE(21,20) NOT NULL " +
    				") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;";
    		Statement statement = createConnection().createStatement();
    		int rs = statement.executeUpdate(query);
    		System.out.println("create pagerank table: " + rs);
    		statement.close();
    		
    		// insert values
    		query = "INSERT INTO pagerank VALUES (?, ?)";
    		PreparedStatement ps = createConnection().prepareStatement(query);
    		
    		for (int i = 0; i < this.entries.length; i++) {
    			ps.setInt(1, this.entries[i]);
    			ps.setDouble(2, this.pageMat.get(0, i));
    			ps.addBatch();
    			ps.clearParameters();
    		}
    		
    		int batchRes = 1;
    		int[] tmpRes = ps.executeBatch();
			for (int r : tmpRes) {
				batchRes *= r;
			}
			System.out.println("Insert pagerank items (" + tmpRes.length + ") result: " + batchRes);
			ps.close();
    		
    	}
    	catch (Exception e) {
    		System.out.println("updateToDB: " + e);
    	}
    }
    
    public Matrix getPageRank() {
    	return this.pageMat;
    }
    
    public Integer[] getEntries() {
    	return this.entries;
    }

}
