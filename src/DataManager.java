import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;

import org.mariadb.jdbc.Driver;

public class DataManager {
	private String host, username, password, driver;
	
	public DataManager(String host, String username, String password, String driver) {
		this.host = host;
		this.username = username;
		this.password = password;
//		this.driver = "com.mariadb.jdbc.Driver";
		
		this.init();
	}
	
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Connection connection;
 
        connection = DriverManager.getConnection(host, username, password);
 
        return connection;
    }
    
    public void init() {
    	try {
    		// wiki
    		int rs = this.queryFromFile("wiki.sql");    		
    		System.out.println("wiki.sql processed " + rs);
    		
    		rs = this.queryFromFile("link.sql");    		
    		System.out.println("link.sql processed " + rs);
    		
			this.createInversedIndexTable();
			this.makeInversedIndexAndCalcTfIdf();
    		
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }
    
    public int queryFromFile(String path) {
    	try {
    		// wiki
    		
    		InputStream in = this.getClass().getClassLoader().getResourceAsStream(path);
    		StringBuffer fileContents = new StringBuffer();
    		
    		Scanner sc = new Scanner(in);
			String line;
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				fileContents.append(line);
			}
    		
    		String query = fileContents.toString();
    		Statement statement = createConnection().createStatement();
    		int rs = statement.executeUpdate(query);
    		statement.close();
    		System.gc();
    		
    		return rs;
    	}
    	catch (Exception e) {
    		System.out.println(e);
    		return -1;
    	}
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
    		System.out.println("Inverted Index Table Created " + rs);
    		statement.close();
    		System.gc();
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }
 
    public void makeInversedIndexAndCalcTfIdf() {
        try {
            Statement statement = createConnection().createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM wiki;");
            
            String query = "INSERT INTO inverted_index VALUES (?, ?)";
            
            int batchLimit = 50000;
            int batchCounter = 0;
            int batchRes = 1;
        	PreparedStatement ps = createConnection().prepareStatement(query);
        	
        	Hashtable<String, Integer> nd = new Hashtable<String, Integer>();
        	Hashtable<String, Integer> ndt = new Hashtable<String, Integer>();
        	Hashtable<String, Integer> nt = new Hashtable<String, Integer>();
            
        	int[] tmpRes;
            while (rs.next()) {
            	HashSet<String> termSet = new HashSet<String>(); 
            	String did = rs.getString("id");
            	String[] tokenizedTerms = TokenizerDemo.tokenize(rs.getString("text"));
            	for(String term : tokenizedTerms ) {
            		/* section for inverted index */
            		ps.setString(1, term);
            		ps.setString(2, did);
            		ps.addBatch();
            		ps.clearParameters();
            		batchCounter++;
            		
            		if (batchCounter >= batchLimit) {
            			tmpRes = ps.executeBatch();
            			for (int r : tmpRes) {
            				batchRes *= r;
            			}
            			System.out.println("Inverted Index Table Insert(" + tmpRes.length + ") result: " + batchRes);
            			ps.close();
            			System.gc();
            			
            			ps = createConnection().prepareStatement(query);
            			batchCounter = 0;
            		}
            		
            		/* section for TF-IDF */
            		// n(d,t)
            		String tmpKey = did + "," + term;
            		Integer v = ndt.get(tmpKey);
            		if ( v == null ) {
            			ndt.put(tmpKey, 1);
            		}
            		else {
            			ndt.put(tmpKey, v+1);
            		}
            		// n(t)
            		if ( !termSet.contains(term) ) {
            			v = nt.get(term);
                		if ( v == null ) {
                			nt.put(term, 1);
                		}
                		else {
                			nt.put(term, v+1);
                		}
                		termSet.add(term);
            		}
            		
            		
            	}
            	// n(d)
            	nd.put(did, tokenizedTerms.length);        		
            }
            
          tmpRes = ps.executeBatch();
			for (int r : tmpRes) {
				batchRes *= r;
			}
			System.out.println("Inverted Index Table Insert(" + tmpRes.length + ") result: " + batchRes);
			ps.close();
            
            statement.close();
            System.gc();
            
            /* section for TD-IDF */
            this.createTFIDFTable();
            batchCounter = 0;
            batchRes = 1;
            query = "INSERT INTO tf_idf VALUES (?, ?, ?)";
            ps = createConnection().prepareStatement(query);
            
            Set<String> ndtKeys = ndt.keySet();
//            String tmpKey;
            String[] tmp;
            Integer ndtV, ndV, ntV;
            String doc, term;
            Double tfidf;
            
            
            for(String tmpKey : ndtKeys) {
//            	tmpKey = ndtKeys.nextElement();
            	tmp = tmpKey.split(",", 2);
            	doc = tmp[0];
            	term = (tmp.length > 1) ? tmp[1] : ",";
            	
            	ndtV = ndt.get(tmpKey);
            	ndV = nd.get(doc);
            	ntV = nt.get(term);
            	
            	tfidf = Math.log(1.0 + ndtV / (double)ndV) / ntV;
            	
            	ps.setString(1, term);
        		ps.setString(2, doc);
        		ps.setDouble(3, tfidf);
        		ps.addBatch();
        		ps.clearParameters();
        		batchCounter++;
        		
        		if (batchCounter >= batchLimit) {
        			tmpRes = ps.executeBatch();
        			for (int r : tmpRes) {
        				batchRes *= r;
        			}
        			System.out.println("TF IDF Insert(" + tmpRes.length + ") result: " + batchRes);
        			ps.close();
        			System.gc();
        			
        			ps = createConnection().prepareStatement(query);
        			batchCounter = 0;
        		}
            }
            
            tmpRes = ps.executeBatch();
			for (int r : tmpRes) {
				batchRes *= r;
			}
			System.out.println("TF IDF Insert(" + tmpRes.length + ") result: " + batchRes);
			ps.close();
			System.gc();
            
            
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
    		System.gc();
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
    		System.out.println("TF-IDF Table Created " + rs);
    		statement.close();
    		System.gc();
    	}
    	catch (Exception e) {
    		System.out.println(e);
    	}
    }

}
