package socialViz;

import java.util.LinkedHashMap;
import java.util.Map;

import socialViz.hastagHashmap;
import socialViz.generateCooccurence;

public class Main {
	
	private static Map<String, Integer> ht = new LinkedHashMap<String, Integer>();
	
	public static void main(String args[]){
		hastagHashmap hthm = new hastagHashmap();
		ht = hthm.create();
		//Display hashtag list as key-value pair
		//hthm.display(ht);
		
		generateCooccurence gc = new generateCooccurence();
		gc.openConnection();
		gc.getDatesFromDatabase();
		gc.buildMatrix(ht);
		gc.closeConnection();
		}
}
