
public class Runner {

	public static void main(String[] args) {
		try {
			DataManager dm = new DataManager();
			dm.createInversedIndexTable();
			dm.makeInversedIndex();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		

	}

}
