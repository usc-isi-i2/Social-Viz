package socialViz;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/*Create a LinkedHashMap for list of hash tags*/

public class hastagHashmap {
	
	private Map<String, Integer> ht = new LinkedHashMap<String, Integer>();
	
	public Map<String,Integer> create(){
		
		BufferedReader br = null;
		int i = 0;
		
		try{
			br = new BufferedReader(new FileReader("./resources/health_hashtag_list.csv")); 
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
	
	public void display(Map<String,Integer> ht){
		Iterator<Entry<String, Integer>> iter = ht.entrySet().iterator();
		
		while(iter.hasNext()){
			Entry<String, Integer> entry = (Entry<String, Integer>) iter.next();
			System.out.println(entry.getKey()+" "+entry.getValue());
		}
	}
	
}
