


import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
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
import java.util.Collection;
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
			PrintWriter out = response.getWriter ();
			String stDate = request.getParameter("stDate");										//retrieve parameters
			String edDate = request.getParameter("edDate");
			String phoneNum = request.getParameter("phoneNum");
			String maxNode = request.getParameter("maxNode");
			String category = request.getParameter("category");
			
			JSONObject obj = new JSONObject();
			
			QueryElastic qe = new QueryElastic(stDate, edDate, phoneNum, maxNode, category);				//query elastic search
			out.println(qe.getFormattedDate());
		}
		catch (Exception e) {
			e.printStackTrace();
		} 			
	}
	

	
	public class QueryElastic {
		private String start, end, nums, maxN, category, categoryQueryString;
		private JSONObject json;

		public QueryElastic(String stDate, String edDate, String phoneNum, String maxNode, String cate){
			this.start = stDate;
			this.end = edDate;
			this.nums = phoneNum;
			this.maxN = maxNode;
			this.category = cate;
			switch(this.category){
		      	case "age":{
		      		this.categoryQueryString = "hasFeatureCollection.person_age_feature.featureValue";
		      		break;
		      	}
		      	case "ethnicity":{
		      		this.categoryQueryString = "hasFeatureCollection.person_ethnicity_feature.featureValue";
		      		break;
		      	}
		      	case "eyecolor":{
		      		this.categoryQueryString = "hasFeatureCollection.person_eyecolor_feature.featureValue";
		      		break;
		      	}
		      	case "haircolor":{
		      		this.categoryQueryString = "hasFeatureCollection.person_haircolor_feature.featureValue";
		      		break;
		      	}
		      	case "":{
		      		this.categoryQueryString = "dateCreated";
		      		break;
		      	}
		    }
		}
		
		public String getFormattedDate(){
			long st = System.currentTimeMillis();
			String phones = retrieveNum();												//retrieve popular phone numbers
			String count = this.getQueryCount(phones);									//get the number of result 
			String res = queryRes(phones, count);										//get query result of these phone numbers
			long ed = System.currentTimeMillis();
			System.out.println("Query takes " + (ed - st) + " ms");
			this.writeRes(res, "C:\\wamp\\www\\d3Note\\socialVis\\testElastic\\queryRes.json");
			
//			String res = this.readRes();
			
			ElasticToSchema1 elastic = new ElasticToSchema1(res, "http://localhost:8080/queryElastic/servlets/socialVis/testElastic/dataset/newCityGeo.csv", this.category, this.categoryQueryString);          //C:\\wamp\\www\\d3Note\\socialVis\\testElastic\\dataset\\
			System.out.println("Finish retrieve data");
			return elastic.getResult();
		}
		
		private String readRes(){
			String path = "C:\\wamp\\www\\d3Note\\socialVis\\testElastic\\queryRes.json";
			String res = null;
			try {
				InputStream input = new FileInputStream(new File(path));
				BufferedReader br = new BufferedReader(new InputStreamReader(input));
				res = br.readLine();
				br.close();
			} catch (Exception e){
				e.printStackTrace();
			}
			return res;
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
		
		public String queryRes(String phones, String count){											//start query based on the passing in parameters
			String query = buildSearchQuery(phones, count);									//builde query string with phone numbers
			System.out.println(query);
			try {
				JSONObject json = executeQuery(query);
				return json.toString();
			} catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}
		
		public String buildSearchQuery(String phones, String count){									//build query string 
			StringBuilder sb = new StringBuilder();
			String part0 = "{\"fields\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\",\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\",\"hasFeatureCollection.phonenumber_feature.phonenumber\", \"dateCreated\",\"hasFeatureCollection.uri\"";
			String part1 = "],\"query\": {\"filtered\": {\"query\": {\"terms\": {\"hasFeatureCollection.phonenumber_feature.phonenumber\": [";
			String part2 = "]}}, \"filter\": {\"and\": {\"filters\": [{\"exists\": {\"field\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\"]}},{\"exists\": {\"field\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\"]}},{\"exists\": {\"field\": [\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"exists\": {\"field\": [\"" + this.categoryQueryString + "\"]}},{\"range\": {\"dateCreated\": {\"gte\": \"";
			String part3 = "\",\"lte\": \"";
			String part4 = "\"}}}]}}}},\"sort\": { \"dateCreated\": { \"order\": \"asc\" }},\"size\": ";
			String part5 = "}";
			sb.append(part0);
			if (!this.category.equals("")){
				sb.append(",\"" + this.categoryQueryString + "\"");
			}
			sb.append(part1);
			sb.append(phones);
			sb.append(part2);
			sb.append(this.start);
			sb.append(part3);
			sb.append(this.end);
			sb.append(part4);
			sb.append(count);
			sb.append(part5);
			return sb.toString();
		}
		
		//get the number of query result
		public String getQueryCount(String phones){
			String query = this.buildCountQuery(phones);
			System.out.println(query);
			JSONObject json = this.executeQuery(query);
			String res = null;
			try {
				int count = json.getJSONObject("hits").getInt("total");
				res = (count + 100) + "";
			} catch (Exception e){
				e.printStackTrace();
			}
			return res;
		}
		
		//build count query to retrieve number of search query result
		public String buildCountQuery(String phones){
			StringBuilder sb = new StringBuilder();
			String part1 = "{\"fields\": [],\"query\": {\"filtered\": {\"query\": {\"terms\": {\"hasFeatureCollection.phonenumber_feature.phonenumber\": [";
			String part2 = "]}}, \"filter\": {\"and\": {\"filters\": [{\"exists\": {\"field\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\"]}},{\"exists\": {\"field\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\"]}},{\"exists\": {\"field\": [\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"exists\": {\"field\": [\"" + this.categoryQueryString + "\"]}},{\"range\": {\"dateCreated\": {\"gte\": \"";
			String part3 = "\",\"lte\": \"";
			String part4 = "\"}}}]}}}},\"sort\": { \"dateCreated\": { \"order\": \"asc\" }},\"size\": 0}";
			sb.append(part1);
			sb.append(phones);
			sb.append(part2);
			sb.append(this.start);
			sb.append(part3);
			sb.append(this.end);
			sb.append(part4);
			return sb.toString();
		}
		
		public String retrieveNum(){													//retrieve phone numbers by the input numbers or by aggregation query
			List<String> res = new ArrayList<String>();
			if (this.nums == null || this.nums.length() == 0){														//aggregation query
				String query = this.buildAggQuery();
				System.out.println(query);
				JSONObject json = executeQuery(query);
//				System.out.println(json.toString());
				parsePopularNums(res, json, Integer.parseInt(this.maxN));
			} else {																	//passing in parameter of phone numbers
				String[] tmp = this.nums.split("/");
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
//					if (cand.charAt(0) != '+')
//						continue;
					if (cand.length() < 10)
						continue;
						res.add(cand);
						count++;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	 	public String buildAggQuery(){													//build aggregation query string
	 		String part1 = "{\"fields\": [],\"query\": {\"filtered\": {\"filter\": {\"and\": {\"filters\": [{\"exists\": {\"field\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion\"]}},{\"exists\": {\"field\": [\"hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality\"]}},{\"exists\": {\"field\": [\"hasFeatureCollection.phonenumber_feature.uri\"]}},{\"exists\": {\"field\": [\"" + this.categoryQueryString + "\"]}},{\"range\": {\"dateCreated\": {\"gte\": \"";
	 		String part2 = "\",\"lte\":\"";
			String part3 = "\"}}}]}}}},\"aggs\": {\"popular_phones\": {\"terms\": {\"field\": \"hasFeatureCollection.phonenumber_feature.phonenumber\",\"size\":";
			String part4 = "}}},\"size\": 0}";
			StringBuilder sb = new StringBuilder();
			int nodeSize = Integer.parseInt(this.maxN) * 3;
			sb.append(part1);
			sb.append(this.start);
			sb.append(part2);
			sb.append(this.end);
			sb.append(part3);
			sb.append(nodeSize);
			sb.append(part4);
			return sb.toString();
		}
		
		
		private JSONObject executeQuery(String query){									//execute query throw HTTP request
			try {
				String authen = "ZGFycGFtZW1leDpkYXJwYW1lbWV4";
				HttpsURLConnection con = (HttpsURLConnection) new URL("https://esc.memexproxy.com/dig-latest/WebPage/_search").openConnection();
				
				con.setHostnameVerifier(new HostnameVerifier(){

					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						// TODO Auto-generated method stub
						return true;
					}
					
				});
				
				
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
	
	
	public class ElasticToSchema1{
		private String queryRes, geoPath, category, categoryKey;
		private JSONObject result = new JSONObject();
		private Map<String, List<Double>> geoLoc = new HashMap<String, List<Double>>();
		private Map<String, Node> nodeMap = new HashMap<String, Node>();
		private Map<String, Cluster> clusterMap = new HashMap<String, Cluster>();
		private Set<String> dateSet = new HashSet<String>();
		private Set<String> misGeo = new HashSet<String>();
		private Map<String, Integer> categoryMap = new HashMap<String, Integer>();
		
		public ElasticToSchema1(String queryRes, String geoPath, String cate, String cateKey){
			this.queryRes = queryRes;
			this.geoPath = geoPath;
			this.category = cate;
			this.categoryKey = cateKey;
			this.loadGeo(this.geoPath);
		}
		
		public String getResult(){
			long st = System.currentTimeMillis();
			this.loadQueryRes();
			this.formatJsonRes();			
			long ed = System.currentTimeMillis();
			System.out.println("Format data takes " + (ed - st) + " ms");
			
			for (String tmp : this.misGeo)
				System.out.println(tmp);
			return this.result.toString();
		}
		
		private void formatJsonRes(){
			List<String> sortDate = new ArrayList<String>(this.dateSet);
			Collections.sort(sortDate);
			try {
				this.result.put("category", this.category);
				
				JSONArray dateAry = new JSONArray();
				for (String tmp : sortDate){
					dateAry.put(tmp);
				}
				this.result.put("dates", dateAry);
				
				JSONArray cltAry = new JSONArray();
				Comparator<Cluster> comp = new Comparator<Cluster>(){
					public int compare(Cluster c1, Cluster c2){
						return c1.id - c2.id;
					}
				};
				List<Cluster> tmpClusters = new ArrayList<Cluster>(this.clusterMap.values());
				Collections.sort(tmpClusters, comp);
				for (Cluster clt : tmpClusters){
					JSONObject cltObj = new JSONObject();
					cltObj.put("id", clt.id);
					cltObj.put("group", clt.group);
					cltObj.put("state", clt.state);
					cltObj.put("city", clt.city);
					cltObj.put("lat", clt.lat);
					cltObj.put("lon", clt.lon);
					cltAry.put(cltObj);
				}
				this.result.put("clusters", cltAry);
				
				JSONArray nodeAry = new JSONArray();
				Comparator<Node> comp1 = new Comparator<Node>(){
					public int compare(Node n1, Node n2){
						return n1.id - n2.id;
					}
				};
				List<Node> tmpNodes = new ArrayList<Node>(this.nodeMap.values());
				Collections.sort(tmpNodes, comp1);
				for (Node node : tmpNodes){
					JSONObject nodeObj = new JSONObject();
					nodeObj.put("label", node.label);
					nodeObj.put("id", node.id);
					nodeObj.put("color", node.color);
					nodeObj.put("category", node.nodeCategory);
					JSONArray appearAry = new JSONArray();
					JSONArray changeTimes = new JSONArray();
					JSONArray appearTimes = new JSONArray();
					int pre = -1, count = 0, preTime = 0;
					for (int i = 0; i < sortDate.size(); i++){
						if (node.appear.containsKey(sortDate.get(i)))
							appearAry.put(node.appear.get(sortDate.get(i)));
						else appearAry.put(pre);
						
						int last = appearAry.getInt(appearAry.length() - 1);
						if (last != pre && last != -1){
							pre = last;
							count++;
						}
						changeTimes.put(count);
						
						if (node.appearTimes.containsKey(sortDate.get(i))){
							preTime += node.appearTimes.get(sortDate.get(i));
						}
						appearTimes.put(preTime);
					}
					nodeObj.put("changeTimes", changeTimes);
					nodeObj.put("appear", appearAry);
					nodeObj.put("appearTimes", appearTimes);
					nodeAry.put(nodeObj);
				}
				this.result.put("nodes", nodeAry);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		private void loadQueryRes(){
			JSONObject obj = null;
			try {
				JSONObject queryRes = new JSONObject(this.queryRes);
				JSONArray res = queryRes.getJSONObject("hits").getJSONArray("hits");
				for (int i = 0; i < res.length(); i++){
					obj = res.getJSONObject(i).getJSONObject("fields");
					String tmpDate = obj.getJSONArray("dateCreated").getString(0);
					String date = tmpDate.substring(0, tmpDate.indexOf("T"));
					String state = obj.getJSONArray("hasFeatureCollection.place_postalAddress_feature.featureObject.addressRegion").getString(0);
					String city = obj.getJSONArray("hasFeatureCollection.place_postalAddress_feature.featureObject.addressLocality").getString(0);
					String categoryVal = (this.category.equals("")) ? "" : obj.getJSONArray(this.categoryKey).get(0).toString();
					String phone = obj.getJSONArray("hasFeatureCollection.phonenumber_feature.phonenumber").getString(0);
					if (!this.dateSet.contains(date))
						this.dateSet.add(date);
					String key = state.toLowerCase() + "/" + city.toLowerCase();
					if (!this.clusterMap.containsKey(key)){
						if (!this.geoLoc.containsKey(key)){
							String tmpKey = state + " / " + city;
							if (!this.misGeo.contains(tmpKey))
								this.misGeo.add(tmpKey);
							continue;
						}
						List<Double> geo = this.geoLoc.get(key);
						Cluster clt = new Cluster(state, city, this.clusterMap.size(), geo.get(0), geo.get(1));
						this.clusterMap.put(key, clt);
					}
					int cltId = this.clusterMap.get(key).id;
					if (!this.nodeMap.containsKey(phone)){
						int color = cltId;
						if (!this.category.equals("")){
							color = getCategoryColor(this.category, categoryVal);
						}
						Node node = new Node(phone, this.nodeMap.size(), color, categoryVal);
						this.nodeMap.put(phone, node);
					}
					Node node = this.nodeMap.get(phone);
					if (!node.appear.containsKey(date)){
						node.appear.put(date, cltId);
						node.appearTimes.put(date, 1);
					} else {
						node.appearTimes.put(date, node.appearTimes.get(date) + 1);
					}
				}
			} catch (Exception e){
				System.out.println(obj.toString());
				e.printStackTrace();
			}
		}
		
		private int getCategoryColor(String category, String val){									//set color based on category
			if (category.equals("age")){
				int res = -1;
				try {
					res = Integer.parseInt(val) / 5;
				} catch (Exception e){
					e.printStackTrace();
				}
				return res;
			} else {
				if (this.categoryMap.containsKey(val)){
					return this.categoryMap.get(val);
				} else {
					int res = this.categoryMap.size();
					this.categoryMap.put(val, res);
					return res;
				}
			}
		}
		
		private void loadGeo(String path){															//load city to Geo location mapping from cityGeo.csv
			int count = 0;
			try {				
				URL url = new URL(path);
	            URLConnection conn = url.openConnection();
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line = "";
				while ((line = br.readLine()) != null){
					count++;
					String[] tmp = line.split(",");
					String state = tmp[0].toLowerCase();
					String city = tmp[1].toLowerCase();
					double lat = Double.parseDouble(tmp[2]);
					double lon = Double.parseDouble(tmp[3]);
					String key = state.toLowerCase() + "/" + city.toLowerCase();					
					if (!this.geoLoc.containsKey(key)){
						List<Double> geo = new ArrayList<Double>();
						geo.add(lat);
						geo.add(lon);
						this.geoLoc.put(key, geo);
					}
				}
				
				br.close();
			} catch(Exception e){
				e.printStackTrace();
				System.out.println("Wrong cityGeo entry at " + count);
			}
		}
		
		class Node{
			int id, color;
			String label, nodeCategory;
			Map<String, Integer> appear = new HashMap<String, Integer>();
			Map<String, Integer> appearTimes = new HashMap<String, Integer>();
			Node(String l, int i, int c, String cate){
				label = l;
				id = i;
				color = c;
				nodeCategory = cate;
			}
		}
		
		class Cluster{
			String state, city;
			int id, group;
			Double lat, lon;
			Cluster(String s, String c, int i, double la, double lo){
				state = s;
				city = c;
				id = i;
				group = i;
				lat = la;
				lon = lo;
			}
		}
		
	}
	
	
}