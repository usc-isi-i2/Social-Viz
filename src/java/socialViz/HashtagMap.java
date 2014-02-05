package socialViz;

/**
 * @author Vaishnavi Dalvi
 * Create a LinkedHashMap for list of hash tags
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HashtagMap {
	
	private Map<String, Integer> ht = new LinkedHashMap<String, Integer>();
	
	/**
	 * Create Hashmap for hashtag list in the form <hastag, identifier>
	 * @return : Returns the Hashmap
	 */
	public Map<String,Integer> create(){
		
		BufferedReader br = null;
		int i = 0;
		
		try{
			br = new BufferedReader(new FileReader(AppProperties.getInstance().getHashtagFile())); 
			String line;
			while((line = br.readLine()) != null){
				line=line.trim().toLowerCase();
				if(!ht.containsKey(line)){
					ht.put(line, i);
					i++;
				}
			}
			
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try{
				if(br != null)
					br.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		//display();
		return ht;
	}
	
	/**
	 * Display Hashmap as key-value pair
	 * @param ht : Hashmap to be displayed
	 */
	public void display(Map<String,Integer> ht){
		Iterator<Entry<String, Integer>> iter = ht.entrySet().iterator();
		
		while(iter.hasNext()){
			Entry<String, Integer> entry = (Entry<String, Integer>) iter.next();
			System.out.println(entry.getKey()+" "+entry.getValue());
		}
	}
	
}