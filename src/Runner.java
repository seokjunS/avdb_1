
public class Runner {

	public static void main(String[] args) {
		try {
			DataManager dm = new DataManager();
			dm.createInversedIndexTable();
			dm.makeInversedIndex();
			dm.setIndexToInvertedIndex();
			
			dm.createTFIDFTable();
			
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		

	}

}
