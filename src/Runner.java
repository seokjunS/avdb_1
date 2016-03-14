
public class Runner {

	public static void main(String[] args) {
		try {
			System.out.println("sentence");
			TokenizerDemo.run(args);
			DataManager dm = new DataManager();
			
			dm.runSqlStatement();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		

	}

}
