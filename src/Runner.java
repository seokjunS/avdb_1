import Jama.Matrix;

public class Runner {

	public static void main(String[] args) {
		// setup
		String host = "jdbc:mariadb://147.46.15.238:3306/ADB-2016-21211?allowMultiQueries=true";
		String username = "ADB-2016-21211";
		String password = "ADB-2016-21211";
		String driver = "com.mariadb.jdbc.Driver";
		
		try {
//			DataManager dm = new DataManager(host, username, password, driver);
//			dm.createInversedIndexTable();
//			dm.makeInversedIndexAndCalcTfIdf();
//			dm.setIndexToInvertedIndex();
			
			PageRank pr = new PageRank(host, username, password, driver);
						
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		

	}

}
