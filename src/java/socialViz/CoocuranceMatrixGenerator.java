package socialViz;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generate JSON files by working on project data in MySQL database
 * @author Vaishnavi Dalvi
 *
 */
public class CoocuranceMatrixGenerator {
	
	private Connection conn = null;
	private Date dates[];
	private int daily_max_count=0;
	private int cum_max_count=0;
	AppProperties prop;
	
	public CoocuranceMatrixGenerator() {
		try{
			prop = AppProperties.getInstance();
		} catch(Exception e){
			System.out.println("Failed to load properties");
			e.printStackTrace();
		}
	}
	/**
	 * Getter for maximum count for daily data 
	 * @return : Returns maximum count for daily data
	 */
	public int getDaily_max_count() {
		return daily_max_count;
	}

	/**
	 * Getter for maximum count for cumulative data
	 * @return : Returns maximum count for cumulative data
	 */
	public int getCum_max_count() {
		return cum_max_count;
	}

	/**
	 * Open connection with MySQL database
	 */
	public void openConnection(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			
			String conString = "jdbc:mysql://" + prop.getServer() + ":" + prop.getPort() + "/" + prop.getDatabase();
			conn = DriverManager.getConnection(conString, prop.getUsername(), prop.getPassword());
			System.out.println("Connected..");
		}
		catch(Exception e){
			System.out.println("Connection failed!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Close result and statement 
	 * @param result : ResultSet
	 * @param stmt : Statement
	 */
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
	
	/**
	 * Close database connection
	 */
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
	
	/**
	 * Get all dates from tweets data and store in an array
	 */
	public void getDatesFromDatabase(){
		try {
			Statement stmt = conn.createStatement();
			String query = "select count(distinct " + prop.getCreationDateColumn() + ") from " + prop.getTablename();
			ResultSet result = stmt.executeQuery(query);
			result.next();
			int numDates = result.getInt(1);
			System.out.println("Number of dates : "+numDates);
			dates = new Date[numDates];
			query = "select distinct " + prop.getCreationDateColumn() + " from " + prop.getTablename() +
						" order by " + prop.getCreationDateColumn();
			result = stmt.executeQuery(query);
			int i=0;
			

			while(result.next()){
				Date date = result.getDate(prop.getCreationDateColumn());
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
	
	/**
	 * Build daily as well as cumulative matrix for each date and write to JSON file
	 * @param ht : Hashmap for hashtags
	 */
	public void buildMatrix(Map<String, Integer> ht){
		System.out.println("Build Matrix");
		
		int hashtag_list_size = ht.keySet().size(); // Number of hashtags considered for dataset, currently 837
		System.out.println("Number of hashtags considered : "+hashtag_list_size);
		int[][] cumulativeMatrix  = new int[hashtag_list_size][hashtag_list_size];
		int matrix[][]  = new int[hashtag_list_size][hashtag_list_size];
		
		int max_value; // Maximum count found for a particular date
		int cum_max_value=0;
		
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
				String query = "select " + prop.getHashtagsColumn() + " from " + prop.getTablename() +
						" where " + prop.getCreationDateColumn() + "=\"" + dates[k] + "\"";
				result = stmt.executeQuery(query);
				
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
					count=0; // Count gives the number of health-related hashtags present in a tweet
					
					//For each object in jsonArray of hashtags for a tweet
					//Extract the hashtag and store each hashtag in an array for hashtags in a tweet and increment occurence for that hashtag
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
							if(cumulativeMatrix[tag_id][tag_id] > cum_max_value)
								cum_max_value = cumulativeMatrix[tag_id][tag_id];
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
							if(cumulativeMatrix[hashtags[m]][hashtags[n]] > cum_max_value)
									cum_max_value = cumulativeMatrix[hashtags[m]][hashtags[n]];
						}
					}
				}// While loop ends => All tweets in a date considered 
				//displayMatrix(ht, hashtag_list_size, matrix);
				obj = getJSONObject(matrix,hashtag_list_size, max_value, dates[k],"Day-Wise Cooccurence");
				System.out.println("Writing co-occurence JSON to file");
				writeJSONToFile(obj, dates[k],"matrix");
				obj = getJSONObject(cumulativeMatrix,hashtag_list_size, cum_max_value, dates[k],"Cumulative Cooccurence");
				System.out.println("Writing cumulative co-occurence JSON to file");
				writeJSONToFile(obj, dates[k],"cumMatrix");

				if(max_value > daily_max_count)
					daily_max_count = max_value;
				
				cum_max_count=cum_max_value;
				
			} catch(Exception e){
				e.printStackTrace();
			}	
			close(result, stmt);
		}//For loop ends => All dates considered
	}
	
	/**
	 * Get JSON object from matrix
	 * @param matrix : Matrix of hashtag co-occurence counts
	 * @param hashtag_list_size : Number of hashtags in tweets data, here 837 
	 * @param max_value : Maximum co-occurence count for a particular date
	 * @param d : Date
	 * @param type : Whether daily co-occurence or cumulative co-occurence
	 * @return
	 */
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
			obj.put("x_axis_label", "Hashtags");
			obj.put("y_axis_label", "Hashtags");
			obj.put("x_axis_range", axis_arr);
			obj.put("y_axis_range", axis_arr);
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
	
	/**
	 * Write JSON object to file.
	 * @param obj : JSONObject to be written to file
	 * @param d : Date 
	 * @param printWhat : Whether it is daily co-occurence or cumulative co-occurence
	 */
	private void writeJSONToFile(JSONObject obj, Date d, String printWhat){
		String filepath="";
		if(printWhat.equals("matrix"))
			filepath = prop.getJsonOutputFolder() + "/Cooccurence/Matrix_"+d;
		if(printWhat.equals("cumMatrix"))
			filepath = prop.getJsonOutputFolder() + "/Cumulative/cumMatrix_"+d;
	
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
	
	/**
	 * Initialize matrix
	 * @param matrix : Matrix to be initialized
	 */
	private void initializeMatrix(int matrix[][]){
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix[0].length;j++)
				matrix[i][j]=0;
	}
	
	/**
	 * Display matrix
	 * @param ht : Hashmap for hashtag list
	 * @param hashtag_list_size : Number of hashtags in dataset
	 * @param matrix : Matrix to be displayed
	 */
//	private void displayMatrix(Map<String, Integer> ht, int hashtag_list_size, int matrix[][]){
//		System.out.println();
//		Iterator<Entry<String, Integer>> iter = ht.entrySet().iterator();
//		while(iter.hasNext()){
//			Entry<String, Integer> entry = iter.next();
//			System.out.print(entry.getKey()+" ");
//		}
//			
//		System.out.println();
//		iter = ht.entrySet().iterator();
//		
//		for(int i=0;i<matrix.length;i++){
//			if(iter.hasNext()){
//				Entry<String, Integer> entry = iter.next();
//				System.out.print(entry.getKey()+" ");
//			}
//			for(int j=0;j<matrix[0].length;j++){
//				System.out.print(matrix[i][j]+"\t");
//			}
//			System.out.println();
//		}
//	}
	
	public void writeCountsToFile(){
		int cooccurence_count = getDaily_max_count();
		int cumulative_count = getCum_max_count();
		
		String countFile = prop.getOutputFolder() + "/maxCount.txt";
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(countFile,"UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.println(cooccurence_count);
		pw.println(cumulative_count);
		pw.close();
	}

	
	public static void main(String[] args) throws Exception {
		Map<String, Integer> ht = new LinkedHashMap<String, Integer>();
		HashtagMap hthm = new HashtagMap();
		ht = hthm.create();	
		CoocuranceMatrixGenerator gc = new CoocuranceMatrixGenerator();
		gc.openConnection();
		gc.getDatesFromDatabase();
		gc.buildMatrix(ht);
		gc.closeConnection();
		gc.writeCountsToFile();
	}
}

