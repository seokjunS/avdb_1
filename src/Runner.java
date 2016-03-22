import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import Jama.Matrix;

public class Runner {

	public static void main(String[] args) {
		// setup
		String host = "jdbc:mariadb://147.46.15.238:3306/ADB-2016-21211?allowMultiQueries=true";
		String username = "ADB-2016-21211";
		String password = "ADB-2016-21211";
		String driver = "com.mariadb.jdbc.Driver";
		
		try {
			System.out.println("On Init...");
			DataManager dm = new DataManager(host, username, password, driver);
			dm.init();
			dm.createInversedIndexTable();
			dm.makeInversedIndexAndCalcTfIdf();
//			dm.setIndexToInvertedIndex();
			
			PageRank pr = new PageRank(host, username, password, driver);
			SearchEngine engine = new SearchEngine(host, username, password, driver);
			
			/* main loop */
			System.out.println("----------------");
			System.out.println("ready to search");
			Scanner sc = new Scanner(System.in);
			String input;
			while (true) {
				input = sc.nextLine();
				engine.search(input);
			}
			
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
	}

}
