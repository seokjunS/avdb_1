import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SearchEngine {
	private String host, username, password;
	
	public SearchEngine(String host, String username, String password, String driver) {
		this.host = host;
		this.username = username;
		this.password = password;
	}
	
	public Connection createConnection() throws ClassNotFoundException, SQLException {
        Connection connection;
        connection = DriverManager.getConnection(host, username, password);
 
        return connection;
    }
	
	/* get input and return form as < 'a', 'b', 'c' > */
	public String inputsToQueryStr(String input) {
		String[] inputs = input.toLowerCase().split("\\s+");
		String res = "''";
		
		for (int i = 0; i < inputs.length; i++) {
			res += ",'" + inputs[i] + "'";
		}
		return res;
	}
	
	public void search(String input) {
		try {
			String inputQuery = this.inputsToQueryStr(input);
			String query = "SELECT tfidf.id, wiki.title, tfidf.allterm, tfidf.sc, pagerank.score, tfidf.sc * pagerank.score AS fscore " +
					"FROM (SELECT id, GROUP_CONCAT(DISTINCT term) as allterm, SUM(score) sc FROM tf_idf " +
					"WHERE LOWER(term) IN ("+inputQuery+") " +
					"GROUP BY id LIMIT 10 " + 
					") tfidf " +
					"LEFT JOIN pagerank ON tfidf.id = pagerank.id " +
					"LEFT JOIN wiki ON tfidf.id = wiki.id " +
					"ORDER BY fscore DESC, tfidf.id ASC; ";
			
			Statement statement = createConnection().createStatement();
	        ResultSet rs = statement.executeQuery(query);
	        
	        String id, title, tfidfScore, pagerankScore, terms;
	        while (rs.next()) {
            	id = rs.getString("id");
            	title = rs.getString("title");
            	terms = rs.getString("allterm");
            	tfidfScore = rs.getString("sc");
            	pagerankScore = rs.getString("score");
            	
//            	System.out.printf("%s, %s, %s, %s\n", id, title, tfidfScore, pagerankScore);
            	System.out.printf("%s, %s, %s, %s, %s\n", id, title, terms, tfidfScore, pagerankScore);
	        }
		}
		catch (Exception e) {
			System.out.println("search: " + e);
		}
	}
}
