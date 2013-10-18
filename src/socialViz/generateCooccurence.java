package socialViz;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class generateCooccurence {
	
	private Connection conn = null;
	private Date dates[];
	
	public void openConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/health_data","root","password");
			System.out.println("Connected..");
		}
		catch(Exception e){
			System.out.println("Connection failed!");
			e.printStackTrace();
		}
	}
	
	public void getDatesFromDatabase(){
		try {
			Statement stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("select count(distinct created_at) from health_tweets");
			result.next();
			int numDates = result.getInt(1);
			System.out.println("Number of dates : "+numDates);
			dates = new Date[numDates];
			result = stmt.executeQuery("select distinct created_at from health_tweets order by created_at");
			int i=0;
			Date date;
			while(result.next()){
				date = result.getDate("created_at");
				dates[i]=date;
				System.out.println(dates[i]);
				i++;
			}
			close(result,stmt);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(ResultSet result, Statement stmt){ 
		try {
			if(result != null)
				result.close();
			if(stmt != null)
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void closeConnection(){
	 try{
		 if(conn != null)
			 conn.close();
		 System.out.println("Connection closed.");
	 }	
	 catch(Exception e){
		 e.printStackTrace();
	 }
	}
	
	public void buildMatrix(Map<String, Integer> ht){
		System.out.println("Build Matrix");
		
		int hashtag_list_size = ht.keySet().size(); // Number of hashtags considered for dataset, currently 837
		System.out.println("Number of hashtags considered : "+hashtag_list_size);
		int[][] cumulativeMatrix  = new int[hashtag_list_size][hashtag_list_size];
		int matrix[][]  = new int[hashtag_list_size][hashtag_list_size];
		
		int max_value; // Maximum count found for a particular date
		
		Statement stmt = null;
		ResultSet result = null;
		
		String json="";
		JSONArray arr = null;
		int jsonArraySize;
		int hashtags[];
		int j,count;
		JSONObject obj = null;
		String tag="";
		int tag_id;
		
		initializeMatrix(cumulativeMatrix);
		
		for(int k=0; k<dates.length;k++){
			//For each date
			System.out.println("\nDate is "+dates[k]);
			try{
				stmt = conn.createStatement();
				result = stmt.executeQuery("select entities_hashtags from health_tweets where created_at=\""+dates[k]+"\"");
				//For each date initialize matrix, set max_count to 0
				initializeMatrix(matrix);
				max_value=0;
				
				while(result.next()){
					// For each tweet on a particular date, do statements in while loop
					
					//Get json for hastags
					json = result.getString(1);
					//System.out.println(json);
					arr = new JSONArray(json);
					//jsonArraySize gives the number of hashtags used in that tweet
					jsonArraySize = arr.length();
					//System.out.println("Number of hashtags in tweet : "+jsonArraySize);
					//Create and maintain an array for hashtags in the tweet
					hashtags = new int[jsonArraySize];
					j=0; //For every tweet start from hashtags[0]
					
					//For each object in jsonArray of hashtags for a tweet
					//Extract the hashtag and store each hashtag in an array for hashtags in a tweet and increment occurence for that hashtag
					count=0; // Count gives the number of health-related hashtags present in a tweet
					
					for(int i=0; i<jsonArraySize; i++){
						obj = arr.getJSONObject(i);
						tag = (String) obj.get("text");
						tag=tag.toLowerCase();
						//System.out.println("Tag is "+tag);
						if(ht.containsKey(tag))
						{	
							tag_id=ht.get(tag);
							//System.out.println("Tag_id "+tag_id);
							hashtags[j++]=tag_id;
							matrix[tag_id][tag_id]++;
							cumulativeMatrix[tag_id][tag_id]++;
							if(matrix[tag_id][tag_id] > max_value)
								max_value = matrix[tag_id][tag_id];
							count++;
						}	
					}
					
					//System.out.println("Number of health hashtags present: "+ count);
					//Find co-occurrence
					for(int m=0;m<count-1;m++){
						for(int n=m+1;n<count;n++){
							//System.out.println(hashtags[m]+" "+hashtags[n]);
							matrix[hashtags[m]][hashtags[n]]++;
							matrix[hashtags[n]][hashtags[m]]++;
							cumulativeMatrix[hashtags[m]][hashtags[n]]++;
							cumulativeMatrix[hashtags[n]][hashtags[m]]++;
							if(matrix[hashtags[m]][hashtags[n]] > max_value)
								max_value = matrix[hashtags[m]][hashtags[n]];
						}
					}
				}// While loop ends => All tweets in a date considered 
				//displayMatrix(ht, hashtag_list_size, matrix);
				obj = getJSONObject(matrix,hashtag_list_size, max_value, dates[k],"Day-Wise Cooccurence");
				System.out.println("Writing co-occurence JSON to file");
				writeJSONToFile(obj, dates[k],"matrix");
				obj = getJSONObject(cumulativeMatrix,hashtag_list_size, max_value, dates[k],"Cumulative Cooccurence");
				System.out.println("Writing cumulative co-occurence JSON to file");
				writeJSONToFile(obj, dates[k],"cumMatrix");
			} catch(Exception e){
				e.printStackTrace();
			}	
			close(result, stmt);
		}//For loop ends => All dates considered
	}
	
	private void writeJSONToFile(JSONObject obj, Date d, String printWhat){
		String filepath="";
		if(printWhat.equals("matrix"))
			filepath="./resources/jsonOutput/Cooccurence/Matrix_"+d;
		if(printWhat.equals("cumMatrix"))
			filepath="./resources/jsonOutput/Cumulative/cumMatrix_"+d;
	
		try {
			FileWriter fw = new FileWriter(filepath+".json");
			try {
				fw.write(obj.toString(2));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			obj = null;
			d =null;
		}
		
		
	}
	
	private JSONObject getJSONObject(int matrix[][], int hashtag_list_size, int max_value, Date d, String type)
	{
		JSONObject obj = new JSONObject();
		JSONArray axis_arr = new JSONArray();
		axis_arr.put(0);
		axis_arr.put(hashtag_list_size-1);
		JSONArray intensity_arr = new JSONArray();
		intensity_arr.put(0);
		intensity_arr.put(max_value);
		try {
			obj.put("title","hashtag_vs_hashtag");
			obj.put("x-axis_label", "Hashtags");
			obj.put("y-axis_label", "Hashtags");
			obj.put("x-axis_range", axis_arr);
			obj.put("y-axis_range", axis_arr);
			obj.put("intensity_range",intensity_arr);
			obj.put("Date",d);
			obj.put("Type",type);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONArray data = new JSONArray();
		
		for(int i=0; i<matrix.length; i++){
			for(int j=0;j<matrix[0].length;j++){
				if(matrix[i][j] != 0){
					JSONObject data_point = new JSONObject();
					try {
						data_point.put("x_position", i);
						data_point.put("y_position", j);
						data_point.put("count", matrix[i][j]);
						data.put(data_point);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}	
		}
		
		try {
			obj.put("Data", data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}
	
	private void initializeMatrix(int matrix[][]){
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[0].length;j++)
				matrix[i][j]=0;
	}
	
	private void displayMatrix(Map<String, Integer> ht, int hashtag_list_size, int matrix[][]){
		System.out.println();
		Iterator<Entry<String, Integer>> iter = ht.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String, Integer> entry = iter.next();
			System.out.print(entry.getKey()+" ");
		}
			
		System.out.println();
		iter = ht.entrySet().iterator();
		
		for(int i=0;i<matrix.length;i++){
			if(iter.hasNext()){
				Entry<String, Integer> entry = iter.next();
				System.out.print(entry.getKey()+" ");
			}
			for(int j=0;j<matrix[0].length;j++){
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
		}
	}
}

