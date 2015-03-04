


import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.io.*;
import java.util.*;

import org.json.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class queryServlet extends HttpServlet {
	protected void doGet (HttpServletRequest request,HttpServletResponse response)throws IOException,HttpRetryException {			
		try{	
			response.setContentType("application/json; charset=utf-8");
//			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter ();
			String stDate = request.getParameter("stDate");										//retrieve parameters
			String edDate = request.getParameter("edDate");
			String phoneNum = request.getParameter("phoneNum");
			String maxNode = request.getParameter("maxNode");
			
			JSONObject obj = new JSONObject();
//			obj.put("start date", stDate);
//			obj.put("end date", edDate);
//			obj.put("phoneNum", phoneNum);
//			obj.put("maxNode", maxNode);
//			out.println(obj.toString());
			
			QueryElastic qe = new QueryElastic(stDate, edDate, phoneNum, maxNode);				//query elastic search
//			obj.put("res", qe.getFormattedDate());
			out.println(qe.getFormattedDate());
		}
		catch (Exception e) {
			e.printStackTrace();
		} 			
	}
	
//	public static void main(String[] args){
//		queryServlet qs = new queryServlet();
//	}
//	
//	public queryServlet(){
//		QueryElastic qe = new QueryElastic("2014-07-01", "2014-07-31", "", "120");
//		String res = qe.getFormattedDate();
//		System.out.println(res);
//	}
	
	public class ElasticToSchema {
		private String queryRes, geoPath;
		private JSONObject json = new JSONObject();																		//the formatted result
		private Map<String, Geo> geo = new HashMap<String, Geo>();														//store Geo locations
		private Map<String, JSONObject> clusters = new HashMap<String, JSONObject>();									//store clusters
		private Map<String, Integer> nodes = new HashMap<String, Integer>();											//store node label to id mapping
		private Map<String, HashMap<String, JSONObject>> map = new HashMap<String, HashMap<String, JSONObject>>();		//map<date, map<node label, node object>>
		private Set<String> missingLoc = new HashSet<String>();															//cities not covered by cityGeo.csv
		
		public ElasticToSchema(String queryRes, String geoPath){
			this.queryRes = queryRes;
			this.geoPath = geoPath;
		}
		
		public JSONObject getFormattedDate(){
			loadGeo(geoPath);
			try {
				JSONObject obj = new JSONObject(queryRes); 
				readNode(obj);
				adjustMap();
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
		
		private void buildJSON(){																	//retrieve data from map and convert it to schema format
			try {
				JSONArray daily = new JSONArray();
				JSONArray clt = new JSONArray();
				List<JSONObject> tmpDaily = new ArrayList<JSONObject>();
				
				for (Map.Entry<String, HashMap<String, JSONObject>> et : this.map.entrySet()){
					JSONObject obj = new JSONObject();
					obj.put("Date", et.getKey());
					
					int[] groupCount = new int[this.clusters.size()];
					JSONArray ary = new JSONArray();
					JSONArray aryCount = new JSONArray();
					Map<String, JSONObject> tmpMap = et.getValue();
					System.out.println(et.getKey() + " " + tmpMap.size());
					for (Map.Entry<String, JSONObject> et1 : tmpMap.entrySet()){
						JSONObject tmp = et1.getValue();
						ary.put(tmp);
						groupCount[tmp.getInt("cluster")]++;
					}
					for (int num : groupCount){
						aryCount.put(num);
					}
					obj.put("groupCount", aryCount);
					obj.put("nodes", ary);
					tmpDaily.add(obj);
				}
				json.put("dailyData", daily);	
				
				Comparator<JSONObject> comp = new Comparator<JSONObject>(){
					public int compare(JSONObject arg0, JSONObject arg1){
						try {
							return arg0.getString("Date").compareTo(arg1.getString("Date"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						return -1;
					}
				};
				Collections.sort(tmpDaily, comp);
				
				for (JSONObject obj : tmpDaily){
					daily.put(obj);
				}
				
				
				
				for (Map.Entry<String, JSONObject> et : this.clusters.entrySet()){
					clt.put(et.getValue());
				}
				json.put("clusters", clt);
				
				
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		private void readNode(JSONObject file){															//extract information from input json string
			try {			
				JSONArray ary = file.getJSONObject("hits").getJSONArray("hits");
				for (int i = 0; i < ary.length(); i++){
					JSONObject obj = ary.getJSONObject(i).getJSONObject("fields");
					String region0 = obj.getJSONArray("hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality").getString(0).toLowerCase();
					String region = region0.replace(" ", "");
					String date = obj.getJSONArray("dateCreated").getString(0);
					date = date.substring(0, date.indexOf("T"));
					String phone = obj.getJSONArray("hasFeatureCollection.phonenumber_feature.phonenumber").getString(0);
					
					if (!this.map.containsKey(date))
						this.map.put(date, new HashMap<String, JSONObject>());
					if (this.map.get(date).containsKey(phone))
						continue;
					
					
					if (!clusters.containsKey(region)){
						JSONObject tmp = new JSONObject();
						tmp.put("id", clusters.size());
						tmp.put("label", region);
						tmp.put("group", clusters.size());
						if (!this.geo.containsKey(region)){
							if (!this.missingLoc.contains(region0))
								this.missingLoc.add(region0);
							continue;
						} else {
							Geo lol = this.geo.get(region);
							tmp.put("long", lol.x);
							tmp.put("lat", lol.y);
						}
						clusters.put(region, tmp);
					}
					
					
					JSONObject tmp = new JSONObject();
					int id = -1;
					if (!this.nodes.containsKey(phone))
						this.nodes.put(phone, this.nodes.size());
					id = this.nodes.get(phone);
					int clusterId = clusters.get(region).getInt("id");
					tmp.put("label", phone);
					tmp.put("id", id);
					tmp.put("color", id);
					tmp.put("cluster", clusterId);
					this.map.get(date).put(phone, tmp);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		private void loadGeo(String path){															//load city to Geo location mapping from cityGeo.csv
			try {
				URL url = new URL(path);
	            URLConnection conn = url.openConnection();
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line = "";
				
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
		


		class Geo{
			double x;
			double y;
			Geo(double a, double b){
				x = a;
				y = b;
			}
		}
	}
	
	public class QueryElastic {
		private String start, end, nums, maxN;
		private JSONObject json;

		public QueryElastic(String stDate, String edDate, String phoneNum, String maxNode){
			start = stDate;
			end = edDate;
			nums = phoneNum;
			maxN = maxNode;
//			try {
//				PrintWriter pw = new PrintWriter("http://localhost:8080/queryElastic/servlets/socialVis/testElastic/dataset/log.txt");
//				pw.println(start + " " + edDate + " " + nums + " " + maxN);
//				pw.close();
//			} catch (Exception e){
//				e.printStackTrace();
//			}
		}
		
		public String getFormattedDate(){
			String phones = retrieveNum();												//retrieve popular phone numbers
			String res = queryRes(phones);												//get query result of these phone numbers
			ElasticToSchema elastic = new ElasticToSchema(res, "http://localhost:8080/queryElastic/servlets/socialVis/testElastic/dataset/cityGeo.csv");          //C:\\wamp\\www\\d3Note\\socialVis\\testElastic\\dataset\\
			this.json = elastic.getFormattedDate();
			return this.json.toString();
		}
		
		public void writeRes(String res, String path){									//write result to disk
			try {
				PrintWriter pw = new PrintWriter(path);
				pw.println(res);
				pw.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		public String queryRes(String phones){											//start query based on the passing in parameters
			String query = buildSearchQuery(phones);									//builde query string with phone numbers
			//System.out.println(query);
			try {
				JSONObject json = executeQuery(query);
				return json.toString();
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		public String buildSearchQuery(String phones){									//build query string 
			StringBuilder sb = new StringBuilder();
			String part1 = "{\"fields\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\",\"hasFeatureCollection.phonenumber_feature.phonenumber\",\"dateCreated\",\"hasFeatureCollection.uri\"],\"query\":{\"filtered\":{\"query\":{\"terms\":{\"hasFeatureCollection.phonenumber_feature.phonenumber\":[";
			String part2 = "]}},\"filter\":{\"and\":{\"filters\":[{\"exists\":{\"field\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\"]}},{\"exists\":{\"field\":[\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\"]}},{\"exists\":{\"field\":[\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"term\":{\"hasFeatureCollection.phonenumber_feature.wasGeneratedBy.wasAttributedTo\":\"http://memex.zapto.org/data/software/extractor/stanford/version/1\"}},{\"range\":{\"dateCreated\":{\"gte\":";
			String part3 = ",\"lte\":";
			String part4 = "}}}]}}}},\"sort\":{\"dateCreated\":{\"order\":\"asc\"}},\"size\":3000}";
			sb.append(part1);
			sb.append(phones);
			sb.append(part2);
			sb.append("\"" + this.start + "\"");
			sb.append(part3);
			sb.append("\"" + this.end + "\"");
			sb.append(part4);
			return sb.toString();
		}
		
		public String retrieveNum(){													//retrieve phone numbers by the input numbers or by aggregation query
			List<String> res = new ArrayList<String>();
			if (this.nums == null || this.nums.length() == 0){														//aggregation query
				String query = this.buildAggQuery();
//				return query;
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
}