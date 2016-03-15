import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

public class TokenizerDemo {

//	public static void run(String[] args) throws IOException {
//		System.out.println(args);
//		for (String arg : args) {
//	      // option #1: By sentence.
//	      DocumentPreprocessor dp = new DocumentPreprocessor(arg);
//	      for (List<HasWord>	 sentence : dp) {
//	        System.out.println(sentence);
//	      }
//	      // option #2: By token
//	      PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(arg),
//	              new CoreLabelTokenFactory(), "");
//	      while (ptbt.hasNext()) {
//	        CoreLabel label = ptbt.next();
//	        System.out.println(label);
//	      }
//	    }
//	}
	
	public static String[] tokenizeUnique(String input) {
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(input), 
				new CoreLabelTokenFactory(), "");
		Set<String> res = new HashSet<String>();
		
		while (ptbt.hasNext()) {
			CoreLabel label = ptbt.next();
	        res.add(label.toString());
		}
		return res.toArray(new String[0]);
	}
	
	public static String[] tokenize(String input) {
		PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new StringReader(input), 
				new CoreLabelTokenFactory(), "");
		List<String> res = new ArrayList<String>();
		
		while (ptbt.hasNext()) {
			CoreLabel label = ptbt.next();
	        res.add(label.toString());
		}
		return res.toArray(new String[res.size()]);
	}
}
