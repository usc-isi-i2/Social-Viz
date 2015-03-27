

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
// import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Servlet implementation class queryServlet
 */
// @WebServlet("/queryServlet")
public class queryServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public queryServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("doGet");
		
		try{	
			response.setContentType("application/json; charset=utf-8");
//			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter ();
			String stDate = request.getParameter("stDate");										//retrieve parameters
			String edDate = request.getParameter("edDate");
			String category = request.getParameter("category");
			//String phoneNum = request.getParameter("phoneNum");
			//String maxNode = request.getParameter("maxNode");
			
			
			String phoneNum = "8582055224";
			String maxNode = "20";
			
			//JSONObject obj = new JSONObject();
//			obj.put("start date", stDate);
//			obj.put("end date", edDate);
//			obj.put("phoneNum", phoneNum);
//			obj.put("maxNode", maxNode);
//			out.println(obj.toString());
			
			QueryElastic qe = new QueryElastic(stDate, edDate, category, maxNode);				//query elastic search
//			obj.put("res", qe.getFormattedDate());
			out.println(qe.getFormattedDate());
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		
		
	}

}



class ElasticToSchema {
	private String queryRes, geoPath;
	private JSONObject json = new JSONObject();																		//the formatted result
	private Map<String, Geo> geo = new HashMap<String, Geo>();														//store Geo locations
	private JSONObject geojson = new JSONObject();
	private Map<String, JSONObject> clusters = new HashMap<String, JSONObject>();									//store clusters
	private Map<String, Integer> nodes = new HashMap<String, Integer>();											//store node label to id mapping
	private Map<String, HashMap<String, JSONObject>> map = new HashMap<String, HashMap<String, JSONObject>>();		//map<date, map<node label, node object>>
	private Set<String> missingLoc = new HashSet<String>();															//cities not covered by cityGeo.csv
	private Map<String, String> colors = new HashMap<String, String>();
	private Map<String, List<JSONObject>> date = new TreeMap<String, List<JSONObject>>();
	
	public ElasticToSchema(String queryRes, String geoPath){
		this.queryRes = queryRes;
		this.geoPath = geoPath;
		
	}
	
	public JSONObject getFormattedDate(){
		
		try {
			JSONObject obj = new JSONObject(queryRes); 
			loadGeo(geoPath);
			buildGeoJSON();
			readNode(obj);
		//	adjustMap();
			buildJSON();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.json;
	}
	
	public void adjustMap(){																	//for a node appears in previous day but not in current day, add it to current day
		List<String> dates = new ArrayList<String>(this.map.keySet());
		Comparator<String> comp = new Comparator<String>(){
			public int compare(String arg0, String arg1){
				return arg0.compareTo(arg1);
			}
		};
		Collections.sort(dates, comp);
		System.out.println(dates);
		for (int i = 0; i < dates.size() - 1; i++){			
			Map<String, JSONObject> curMap = this.map.get(dates.get(i));
			Map<String, JSONObject> nextMap = this.map.get(dates.get(i + 1));
			for (Map.Entry<String, JSONObject> et : curMap.entrySet()){
				if (!nextMap.containsKey(et.getKey()))
					nextMap.put(et.getKey(), et.getValue());
			}
		}
	}
	
	private void buildJSON(){
		try {
			JSONArray output = new JSONArray();
			for (Map.Entry<String, List<JSONObject>> et : this.date.entrySet()){
				JSONObject d = new JSONObject();
				d.put("key", et.getKey());
				d.put("values", et.getValue());
				output.put(d);
			}
			json.put("output", output);
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void readNode(JSONObject file){
		try {			
			HashMap<String, JSONObject> buf = new HashMap<String, JSONObject>();
			JSONArray ary = file.getJSONObject("aggregations").getJSONObject("popular_phones").getJSONArray("buckets");
			JSONObject places = geojson.getJSONObject("places");
			for (int i = 0; i < ary.length(); i++){
				JSONObject obj = ary.getJSONObject(i);
				JSONArray buckets = obj.getJSONObject("place").getJSONArray("buckets");
				String place = obj.getString("key");
				
				for(int j=0; j<buckets.length(); j++){
					JSONObject o = buckets.getJSONObject(j);
					String d = o.getString("key_as_string");
					d = d.substring(0, d.indexOf("T"));
					int count = o.getInt("doc_count");
					JSONArray ethnicity = o.getJSONObject("ethnicity").getJSONArray("buckets");
					for(int k=0; k<ethnicity.length();k++){
						JSONObject ok = ethnicity.getJSONObject(k);
						String race = ok.get("key").toString();
						if(!colors.containsKey(race)){
							String code = ""+(int)(Math.random()*256);
							   code = code+code+code;
							   int  in = Integer.parseInt(code);
							   String color = Integer.toHexString( 0x1000000 | in).substring(1).toUpperCase();
				
							   while(colors.containsValue(color)){
								   
								   String code1 = ""+(int)(Math.random()*256);
								   code1 = code1+code1+code1;
								   int  in1 = Integer.parseInt(code1);
								   color = Integer.toHexString( 0x1000000 | in1).substring(1).toUpperCase();
							   }
							   
							   colors.put(race.toLowerCase(),color);
						}
						int race_count = ok.getInt("doc_count");
						
						if(places.has(place.toLowerCase())){
						JSONObject tmp = new JSONObject();
						tmp.put("value", race_count);
						tmp.put("place", place.toLowerCase());
						tmp.put("series", race.toLowerCase());
						tmp.put("color", "#"+colors.get(race.toLowerCase()));
						if(!date.containsKey(d)){
							date.put(d, new ArrayList<JSONObject>());
						}
						date.get(d).add(tmp);
						}
					}
					
					//buf.put(d, tmp);
				}
			}
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void buildGeoJSON(){
		
		JSONObject geoData = new JSONObject();
		try{
			for (Map.Entry<String, Geo> et : this.geo.entrySet()){
				Geo g = et.getValue();
				JSONObject gjson = new JSONObject();
				gjson.put("lat", g.lat);
				gjson.put("long", g.lon);
				geoData.put(et.getKey().toLowerCase(), gjson);
			}
			
			geojson.put("places", geoData);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void loadGeo(String path){															//load city to Geo location mapping from cityGeo.csv
		try {
			/*URL url = new URL(path);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";*/
			
			URL url = new URL("http://localhost:8080/Gandhali/servlets/socialVis/dataset/cityGeo.csv");
	        URLConnection conn = url.openConnection();
	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			StringBuilder sb = new StringBuilder();
			
			while ((line = br.readLine()) != null){
				String[] tmp = line.split(",");
				tmp[0] = tmp[0].toLowerCase();
				tmp[0] = tmp[0].replace(" ", "");
				double x = Double.parseDouble(tmp[1]);
				double y = Double.parseDouble(tmp[2]);
				geo.put(tmp[0], new Geo(x, y));
			}
			
			br.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}


class Geo{
	double lon;
	double lat;
	Geo(double a, double b){
		lon = a;
		lat = b;
	}
}

class QueryElastic {
	private String start, end, nums, maxN;
	private JSONObject json;

	public QueryElastic(String stDate, String edDate, String category, String maxNode){
		start = stDate;
		end = edDate;
		nums = category;
		switch(category){
			case "age":
				nums = "hasFeatureCollection.person_age_feature.person_age";
				break;
			case "ethnicity":
				nums = "hasFeatureCollection.person_ethnicity_feature.person_ethnicity";
				break;
			case "eyecolor":
				nums = "hasFeatureCollection.person_eyecolor_feature.person_eyecolor";
				break;
			case "haircolor":
				nums = "hasFeatureCollection.person_haircolor_feature.person_haircolor";
		}
		maxN = maxNode;
//		try {
//			PrintWriter pw = new PrintWriter("http://localhost:8080/queryElastic/servlets/socialVis/testElastic/dataset/log.txt");
//			pw.println(start + " " + edDate + " " + nums + " " + maxN);
//			pw.close();
//		} catch (Exception e){
//			e.printStackTrace();
//		}
	}
	
	public String getFormattedDate(){
	
		String res = queryRes();												//get query result of these phone numbers
		ElasticToSchema elastic = new ElasticToSchema(res, "http://localhost:8080/Data/cityGeo.csv");          //C:\\wamp\\www\\d3Note\\socialVis\\testElastic\\dataset\\
		this.json = elastic.getFormattedDate();
		return this.json.toString();
	}
	
	/*public void writeRes(String res, String path){									//write result to disk
		try {
			PrintWriter pw = new PrintWriter(path);
			pw.println(res);
			pw.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}*/
	
	public String queryRes(){											//start query based on the passing in parameters
		String query = buildSearchQuery();									//builde query string with phone numbers
		//System.out.println(query);
		try {
			JSONObject json = executeQuery(query);
			return json.toString();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public String buildSearchQuery(){									//build query string 
		StringBuilder sb = new StringBuilder();
		String q="{\"fields\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\",\""+nums+"\"],\"query\":{\"filtered\":{\"filter\":{\"and\":{\"filters\":[{\"exists\":{\"field\":[\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"exists\":{\"field\":\""+nums+"\"}},{\"range\":{\"dateCreated\":{\"gte\":\""+start+"\",\"lte\":\""+end+"\"}}}]}}}},\"aggs\":{\"popular_phones\":{\"terms\":{\"field\":\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"size\":20},\"aggs\":{\"place\":{\"terms\":{\"field\":\"dateCreated\",\"size\":20},\"aggs\":{\"ethnicity\":{\"terms\":{\"field\":\""+nums+"\",\"size\":20}}}}}}},\"size\":0}";
		//String q1="{\"fields\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\",\"hasFeatureCollection.person_age_feature.person_age\",\"hasFeatureCollection.person_ethnicity_feature.person_ethnicity\"],\"query\":{\"filtered\":{\"filter\":{\"and\":{\"filters\":[{\"exists\":{\"field\":[\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"exists\":{\"field\":\"hasFeatureCollection.person_ethnicity_feature.person_ethnicity\"}},{\"range\":{\"dateCreated\":{\"gte\":\"2013-08-01\",\"lte\":\"2014-07-31\"}}}]}}}},\"aggs\":{\"popular_phones\":{\"terms\":{\"field\":\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"size\":20},\"aggs\":{\"place\":{\"terms\":{\"field\":\"dateCreated\",\"size\":20},\"aggs\":{\"ethnicity\":{\"terms\":{\"field\":\"hasFeatureCollection.person_ethnicity_feature.person_ethnicity\",\"size\":20}}}}}}},\"size\":0}";
		String part1 = "{\"fields\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\",\"hasFeatureCollection.phonenumber_feature.phonenumber\",\"dateCreated\",\"hasFeatureCollection.person_ethnicity_feature.person_ethnicity\",\"hasFeatureCollection.uri\"],\"query\":{\"filtered\":{\"filter\":{\"and\":{\"filters\":[{\"exists\":{\"field\":[\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"exists\":{\"field\":\"hasFeatureCollection.person_ethnicity_feature.person_ethnicity\"}},";
		String part2 = "]}},\"filter\":{\"and\":{\"filters\":[{\"exists\":{\"field\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\"]}},{\"exists\":{\"field\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\"]}},{\"exists\":{\"field\":[\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"term\":{\"hasFeatureCollection.phonenumber_feature.wasGeneratedBy.wasAttributedTo\":\"http://memex.zapto.org/data/software/extractor/stanford/version/1\"}},{\"range\":{\"dateCreated\":{\"gte\":";
		String part3 = ",\"lte\":";
		String part4 = "}}}]}}}},\"sort\":{\"dateCreated\":{\"order\":\"asc\"}},\"size\":3000}";
		sb.append(part1);
		//sb.append(phones);
		sb.append(part2);
		sb.append("\"" + this.start + "\"");
		sb.append(part3);
		sb.append("\"" + this.end + "\"");
		sb.append(part4);
		//return sb.toString();
		return q;
	}
	
	public String retrieveNum(){													//retrieve phone numbers by the input numbers or by aggregation query
		List<String> res = new ArrayList<String>();
		if (this.nums == null || this.nums.length() == 0){														//aggregation query
			String query = this.buildAggQuery();
//			return query;
			JSONObject json = executeQuery(query);
			parsePopularNums(res, json, Integer.parseInt(this.maxN));
		} else {																	//passing in parameter of phone numbers
			String[] tmp = this.nums.split("|");
			for (String num : tmp){
				res.add(num);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String phone : res){													//convert List of phone numbers to string
			if (sb.length() > 0)
				sb.append(",");
			sb.append("\"" + phone + "\"");
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public void parsePopularNums(List<String> res, JSONObject json, int size){		//parse the aggregation query result to retrieve popular phone numbers
		int count = 0;
		try {
			JSONArray ary = json.getJSONObject("aggregations").getJSONObject("popular_phones").getJSONArray("buckets");
			int idx = 0;
			while (count < size && idx < ary.length()){
				String cand = ary.getJSONObject(idx++).getString("key");
				if (cand.indexOf("+1-") > -1){
					res.add(cand);
					count++;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
 	public String buildAggQuery(){													//build aggregation query string
		String part1 = "{\"aggs\":{\"popular_phones\":{\"terms\":{\"field\":\"hasFeatureCollection.phonenumber_feature.phonenumber\",\"size\":";
		String part2 = "}}},\"query\":{\"filtered\":{\"filter\":{\"and\":{\"filters\":[{\"exists\":{\"field\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\"]}},{\"exists\":{\"field\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\"]}},{\"exists\":{\"field\":[\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"term\":{\"hasFeatureCollection.phonenumber_feature.wasGeneratedBy.wasAttributedTo\":\"http://memex.zapto.org/data/software/extractor/stanford/version/1\"}},{\"range\":{\"dateCreated\":{\"gte\":\"";
		String part3 = "\",\"lte\":\"";
		String part4 = "\"}}}]}}}},\"size\":0,\"fields\":[]}";
		StringBuilder sb = new StringBuilder();
		int nodeSize = Integer.parseInt(this.maxN) * 3;
		sb.append(part1);
		sb.append(nodeSize);
		sb.append(part2);
		sb.append(this.start);
		sb.append(part3);
		sb.append(this.end);
		sb.append(part4);
		return sb.toString();
	}
	
	
	private JSONObject executeQuery(String query){									//execute query throw HTTP request
		try {
			String authen = "bWVtZXg6ZGlnZGln";
			HttpURLConnection con = (HttpURLConnection) new URL("http://karma-dig-service.cloudapp.net:9090/dig-latest/WebPage/_search").openConnection();
		    con.setDoOutput(true);
		    con.setRequestMethod("POST");
		    con.setRequestProperty("Authorization", "Basic " + authen);
		    OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
			out.write(query); 
			out.close();
			InputStream input = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String response = br.readLine().toString();
			con.disconnect();
			JSONObject json = new JSONObject(response);
			return json;
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
