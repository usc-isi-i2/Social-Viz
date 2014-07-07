import java.io.*;
import java.util.*;

import org.json.*;
//import org.json.simple.*;



public class translationSocialVis {
	int size=20;                                                     //number of file need to read
	double maxWeight=0;                                             //the max weight of nodes
	public ArrayList<String> fileDate=new ArrayList<String>();       //the date of reading file
	ArrayList<Integer> clusterCoordX=new ArrayList<Integer>();       //store the fixed coordinate of cluster center
	ArrayList<Integer> clusterCoordY=new ArrayList<Integer>();
	int K=4;                                                         //number of clusters
	ArrayList<String> hashtag=new ArrayList<String>();               //hashtag of nodes
	ArrayList<Integer> clusterSet=new ArrayList<Integer>();          //the id of cluster
	Map<Integer,node> nodeMap=new HashMap<Integer,node>();          //node's map <id,node>
	ArrayList<node> nodeSet=new ArrayList<node>();                  //nodeSet and edgeSet store node and edge info of current file 
	ArrayList<edge> edgeSet=new ArrayList<edge>();
	Map<Integer,Integer> colorMap=new HashMap<Integer,Integer>();   //assign color to all node based on the last cumulative json file
																	//map<node id, group>
	int [] groupCount;                                              //number of nodes for each group
	
	public class node{
		int indexN;                                                //the rank of degree in all the node
		int id;                                                    //identity/unique mark of node
		int degree;                                                //the total degree of node
		double weight;                                             //the weight of node
		int group;                                                 //give the default group
		ArrayList<Integer> neigId;                                 //the id of neighbor nodes
		int color;                                                 //color of node is decided by the last cumulative file
		node(int i,double w,int d,int g){
			id=i;
			weight=w;
			degree=d;                    
			indexN=-1;                   
			group=g;   
			color=-1;
			neigId=new ArrayList<Integer>();
		}		
		node(){};		
	}
	
	public class edge{
		double value;                                              //the intensity of co-occurrence
		int indexE;                                                //the rank of value of all edges
		node srcNode;                                              //the source node
		node tarNode;                                              //the target node
		edge(node a, node b,double c){
			srcNode=a;
			tarNode=b;
			value=c;
		}
		edge(){}
	}
	
	public static void main(String[] args)	{		
		translationSocialVis A=new translationSocialVis();
	}
	
	translationSocialVis(){
		solution();
	}
	
	//generate the date 
	//and attach date to each json file
	public void createFileDate(){
		for(int i=1;i<=31;i++){                             
			String tmp="March ";
			if(i<10)
				tmp+="0"+i;
			else tmp+=i;
			fileDate.add(tmp);
		}
		for(int i=1;i<=30;i++){
			String tmp="April ";
			if(i<10)
				tmp+="0"+i;
			else if(i<13 || i>=24)
				tmp+=i;
			fileDate.add(tmp);
		}
		for(int i=1;i<=31;i++){
			String tmp="May ";
			if(i<10)
				tmp+="0"+i;
			else tmp+=i;
			fileDate.add(tmp);
		}
		for(int i=1;i<=21;i++){
			String tmp="June ";
			if(i<10)
				tmp+="0"+i;
			else tmp+=i;
			fileDate.add(tmp);
		}
	}
	
	//calculate the position of clusters.
	//In the furture, when K is not set, the position of clusters need an ALG to calculate
	public void computeClusterCoord(){
		/*int width=1346;                                 
		int height=647;
		int sideLen=150; 
		int centerX=width/2;
		int centerY=height/2;				
		 */

		clusterCoordX.add(300);
		clusterCoordX.add(1000);
		clusterCoordX.add(1000);
		clusterCoordX.add(200);
		clusterCoordY.add(150);
		clusterCoordY.add(150);
		clusterCoordY.add(450);
		clusterCoordY.add(450);
	}
	
	//read the label of hashtags, and attach these labels to nodes
	public void readHashtag(){
		try { 		
			String line="";                    
			BufferedReader br=new BufferedReader(new FileReader("C:\\wamp\\www\\d3Note\\socialVis\\health_hashtag_list.csv")); 
			while ((line = br.readLine()) != null) {
				hashtag.add(line);
			} 
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void solution(){
		createFileDate();                     //generate date for reading file
		readHashtag();                        //read hashtag file
		computeClusterCoord();                //generate the coordinate for clusters
		
		readLastCumulative();                     //read the last cumulative file, 
		             		  
		/*clusterSet.add(0);
		clusterSet.add(22);
		clusterSet.add(38);
		clusterSet.add(42);*/
		readCumulative();
		//readCoOccurrence();                   //read number of size co-occurrence file
	}

	//co-occurrence files provide the relationship of nodes in everyday
	public void readCoOccurrence(){           //read co-occurrence file
		String inUrl="C:\\wamp\\www\\d3Note\\socialVis\\Cooccurence\\";
		//String outUrl="D:\\wamp\\www\\d3 note\\socialVis\\formattedCo-Occurrence\\";
		String outUrl="C:\\wamp\\www\\d3Note\\socialVis\\testcooccur\\";
		for(int i=0;i<size;i++){
			clearBuf();                                       //initialize
			
			JSONObject json=readJson(inUrl+i+".json");
			JSONObject output=new JSONObject();
			 
			output=setAndWriteGeneralInfo(json,output,i);             //translate part
			setNodes(json);
			setEdge(json);
			output=writeEdge(output);
			setGroup();
			readColorMap();
			writeNode(output);
			
			writeJson(outUrl+i+".json",output);
			System.out.println("Read "+i+".json finished");
		}
	}
	
	public void readCumulative(){
		String cumulativeInUrl="C:\\wamp\\www\\d3Note\\socialVis\\Cumulative\\";
		String cumulativeOutUrl="C:\\wamp\\www\\d3Note\\socialVis\\formattedCumulative\\";
		
		for(int i=0;i<size;i++){
			clearBuf();                                       //initialize
			
			JSONObject json=readJson(cumulativeInUrl+i+".json");
			JSONObject output=new JSONObject();
			 
			output=setAndWriteGeneralInfo(json,output,i);             //translate part
			setNodes(json);
			setEdge(json);
			output=writeEdge(output);
			setGroup();
			readColorMap();
			writeNode(output);
			
			writeJson(cumulativeOutUrl+i+".json",output);
			System.out.println("Read "+i+".json finished");
		}
	}
	
	//cumulative file provide clusters and node's group information.
	public void readLastCumulative(){             //read the last cumulative file, 
		String cumulativeInUrl="C:\\wamp\\www\\d3Note\\socialVis\\Cumulative\\"+(size-1)+".json";
		String cumulativeOutUrl="C:\\wamp\\www\\d3Note\\socialVis\\formattedCumulative\\cluster.json";
		JSONObject json=readJson(cumulativeInUrl);
		JSONObject output=new JSONObject();
		
		clearBuf();
		setNodes(json);
		setEdge(json);
		setCluster();
		setGroup();
		output=writeCluster(output);
		setColorMap();
		
		
		writeJson(cumulativeOutUrl,output);
		System.out.println("Read cumulative"+(size-1)+".json finished");
	}
	
	//read json from files
	public JSONObject readJson(String inUrl){          
		BufferedReader br = null;	
		String rst="";
		String line="";	
		JSONObject json=new JSONObject();
		try { 			                                    
			br = new BufferedReader(new FileReader(inUrl)); 
			while ((line = br.readLine()) != null) {
				rst+=line;
			} 
			br.close();	
			json=new JSONObject(rst);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return json;		
	}
	
	//write json to files
	public void writeJson(String outUrl,JSONObject output){		
		try {
			FileWriter fw;
			fw = new FileWriter(outUrl);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(output.toString());
			bw.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
	//remove buffer info from previous iteration
	public void clearBuf(){
		nodeMap.clear();
		nodeSet.clear();
		edgeSet.clear();
		//clusterSet.clear();
	}
	
	//general info contains info like the range of node's id, intensity, date of file
	public JSONObject setAndWriteGeneralInfo(JSONObject json,JSONObject output,int num){
		try{
			output.put("title", json.getString("title"));               //read general property from json file
			output.put("Date", json.getString("Date"));
			output.put("Type", json.getString("Type"));
			output.put("x_axis_label", json.getString("x_axis_label"));
			output.put("y_axis_label",json.getString("y_axis_label"));
			output.put("intensity_range", json.getJSONArray("intensity_range"));
			output.put("x_axis_range", json.getJSONArray("x_axis_range"));
			output.put("y_axis_range", json.getJSONArray("y_axis_range"));
			output.put("date", fileDate.get(num));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return output;
	}
	
	//node's degree is how many edges connect to it.
	//node's weight is the sum of weight of connected edges.
	//nodes are sorted based on their weight
	public void setNodes(JSONObject json){
		try{
			JSONArray position=json.getJSONArray("Data");              //get the x,y position json array
			for(int i=0;i<position.length();i++){                      //read position json array
				int xpos=position.getJSONObject(i).getInt("x_position");
				int ypos=position.getJSONObject(i).getInt("y_position");
				int value=position.getJSONObject(i).getInt("count");
				if(xpos!=ypos){                                        //avoid node only has relationship with itself
					if(nodeMap.containsKey(xpos)){
						node tmp=nodeMap.get(xpos);                    
						tmp.degree++;
						tmp.weight+=Math.sqrt(value);
						tmp.neigId.add(ypos);
						nodeMap.put(xpos,tmp);
					}
					else{
						node tmp=new node(xpos,Math.sqrt(value),1,K+1);
						tmp.neigId.add(ypos);
						nodeMap.put(xpos, tmp);
					}
				}
			}	
			
			for(Map.Entry<Integer, node> et:nodeMap.entrySet()){    //parse the tree map, store nodes in the ArrayList for sorting
				nodeSet.add(et.getValue());   
			}
			
			Comparator<node> comparator = new Comparator<node>() {    //define the comparator
			    public int compare(node c1, node c2) {
			        if(c1.weight>c2.weight)
			        	return -1;
			        else if(c1.weight==c2.weight)
			        	return (c1.id-c2.id);
			        else return 1;
			    }
			};
			Collections.sort(nodeSet,comparator);                    //sort node by their degree
			       
			
			for(int i=0;i<nodeSet.size();i++){                      //set index of node by the value of weight descending
				//System.out.println(nodeSet.get(i).id+" "+nodeSet.get(i).degree);
				node tmp=nodeSet.get(i);
				tmp.indexN=i; 			
				nodeSet.set(i, tmp);                               //update nodeSet and nodeMap
				nodeMap.put(tmp.id,tmp);                           
			}			     
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//since the original file contains the situations that edge (a,b) and (b,a), 
	//to remove these cases, just apply a'id<b's id.
	//the weight of edge is square root of intensity, this function can be changed based on the data distribution of intensity.
	//besides, edges are sorted based on the weight, for the using of assign group to nodes
	public void setEdge(JSONObject json){
		try{
			JSONArray position=json.getJSONArray("Data");              //get the x,y position json array
			
			//generate edgesSet
			for(int i=0;i<position.length();i++){                 
				int xpos=position.getJSONObject(i).getInt("x_position");
				int ypos=position.getJSONObject(i).getInt("y_position");
				if(xpos<ypos){
					double value=Math.sqrt(position.getJSONObject(i).getInt("count"));
					node a=nodeMap.get(xpos);
					node b=nodeMap.get(ypos);
					edgeSet.add(new edge(a,b,value));
				}
			}     

			Comparator<edge> comparatorEdge = new Comparator<edge>() {    //define the comparator
			    public int compare(edge c1, edge c2) {
			    	if(c1.value==c2.value)
			    		return (c1.srcNode.id-c2.srcNode.id);
			    	else if(c1.value>c2.value)
			    		return -1;
			    	else return 1;
			    }
			};
			Collections.sort(edgeSet,comparatorEdge);               //sort edge by edge's value
			
			 
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public JSONObject writeEdge(JSONObject output){
		try{
			JSONArray edges=new JSONArray();                           //edges json array
			for(int i=0;i<edgeSet.size();i++){                     //put edgeSet into JSONArray.
				edge tmp=edgeSet.get(i);
				JSONObject tmpEdge=new JSONObject();
				tmpEdge.put("sid", tmp.srcNode.id);
				tmpEdge.put("tid", tmp.tarNode.id);
				tmpEdge.put("source", tmp.srcNode.indexN);
				tmpEdge.put("target", tmp.tarNode.indexN);
				tmpEdge.put("value", tmp.value);
				tmpEdge.put("indexE", i);
				edges.put(tmpEdge);
			}			
			output.put("edges", edges);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return output;
	}
	
	//clusters are chosen in the sequence of node's degree, from high to low.
	//if current node's neighbor's similarity to chosen clusters is less then one threshold, 
	//then choose the node as cluster, and then move to next node. The process stops if K nodes have been chosen.
	public void setCluster(){
		try{ 
			int count=0;                                          //number of cluster choosen
			int index=0;                                          //start index of nodeSet to check proper cluster
			double thresholdCluster=0.5;                          //the threshold of similarity for a node to be chosen as cluster
			
			while(count<K){
				boolean flag=true;
				node cur=nodeSet.get(index++);
				double avg=0;
				for(int i=0;i<clusterSet.size();i++){                //compute similarity to all cluster
					node tmp=nodeMap.get(clusterSet.get(i));
					avg=getSimilarity(cur.neigId,tmp.neigId);
					if(avg>thresholdCluster){
						flag=false;
						break;
					}
				}
				if(flag){
					clusterSet.add(cur.id);
					count++;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public JSONObject writeCluster(JSONObject output){
		try{
			JSONArray clusters=new JSONArray();                   //store clusters
			for(int i=0;i<K;i++){                                 //store the id and coordinate of cluster into JSON
				int id=clusterSet.get(i);
				JSONObject cluster=new JSONObject();				
				node tmpNode=nodeMap.get(id);
				cluster.put("id",tmpNode.id);                   //store node into nodes JSONObject
				cluster.put("indexN", tmpNode.indexN);
				cluster.put("degree", tmpNode.degree);
				cluster.put("weightN", tmpNode.weight);
				cluster.put("group", tmpNode.group);
				cluster.put("label", hashtag.get(tmpNode.id));
				cluster.put("color", tmpNode.color);
				cluster.put("xpos", clusterCoordX.get(i));
				cluster.put("ypos", clusterCoordY.get(i));
				cluster.put("size", groupCount[tmpNode.group-1]);
				clusters.put(cluster);
				System.out.print(tmpNode.indexN+" ");
			}			
			output.put("clusters", clusters);
			System.out.println();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return output;
	}
	
	//set group based on the weight of edge, the method is similar to Kruskal ALG to compute MST.
	//but each time we choose the highest weight edge to insert and stop when only left K clusters
	public void setGroup(){	
		try{
			for(int i=0;i<K;i++){                                 //assign K colors to the K nodes in clusterSet
				int id=clusterSet.get(i);
				node tmpNode=null;
				if(!nodeMap.containsKey(id)){
					tmpNode=new node(id,0,0,-1);
					tmpNode.indexN=nodeSet.size();
					tmpNode.group=i+1;
					nodeSet.add(tmpNode);
				}
				else {
					tmpNode=nodeMap.get(id);
					tmpNode.group=i+1;
					nodeSet.set(tmpNode.indexN, tmpNode);
				}
				nodeMap.put(tmpNode.id, tmpNode);
			}			
			
			groupCount=new int [K];
			int count=0;
			while(count!=edgeSet.size()){                        //stop when all nodes have group
				count=edgeSet.size();
				for(int i=0;i<edgeSet.size();i++){
					edge tmp=edgeSet.get(i);
					node A=tmp.srcNode;
					node B=tmp.tarNode;
					if(A.group!=K+1 && B.group==K+1){           //if one node's group is default, another is not 
						B.group=A.group;                        //give group to the default one and update nodeSet, nodeMap
						nodeSet.set(B.indexN, B);
						nodeMap.put(B.id, B);
						edgeSet.remove(i--);
						groupCount[A.group-1]++;
					}
					else if(A.group==K+1 && B.group!=K+1){
						A.group=B.group;
						nodeSet.set(A.indexN, A);
						nodeMap.put(A.id, A);
						edgeSet.remove(i--);
						groupCount[B.group-1]++;
					}
				}
			}
			for(int i=0;i<K;i++)
				System.out.print(groupCount[i]+" ");
			System.out.println();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	} 
	
	//read node info from nodeSet and store this info into JSON
	public JSONObject writeNode(JSONObject output){
		//get the number of nodes in each group, the numbers will be considered as the weight of cluster
		try{
			JSONArray nodes=new JSONArray();                           //nodes json array
			for(int i=0;i<nodeSet.size();i++){
				JSONObject tmp=new JSONObject();
				node tmpNode=nodeSet.get(i);
				tmp.put("id",tmpNode.id);                   //store node into nodes JSONObject
				tmp.put("indexN", tmpNode.indexN);
				tmp.put("degree", tmpNode.degree);
				tmp.put("weightN", tmpNode.weight);
				tmp.put("group", tmpNode.group);
				tmp.put("label", hashtag.get(tmpNode.id));
				tmp.put("color", tmpNode.color);
				nodes.put(tmp);
				//System.out.println("output  "+tmpNode.id+"  "+tmpNode.weight+"  "+tmpNode.degree);
				/*if(tmpNode.group<=K)
					groupCount[tmpNode.group-1]++;*/
				maxWeight=Math.max(maxWeight, tmpNode.weight);
			}			
			output.put("nodes", nodes);
			output.put("maxCount", maxWeight);
			
			//add the group count info into json file
			JSONArray ary=new JSONArray();
			for(int i=0;i<K;i++){
				ary.put(groupCount[i]);
			}
			output.put("countGroup", ary);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return output;
	}
	
	//cumulative file contains all node and edge from previous co-occurrence file
	//we use the last cumulative file to provide cluster and group for all nodes.
	//So after generate the nodeSet, just put there <id,group> pair into Map for referencing.
	public void setColorMap(){                          
		for(int i=0;i<nodeSet.size();i++){
			node tmp=nodeSet.get(i);
			colorMap.put(tmp.id, tmp.group);
		}
	}
	
	//read the color for nodes from colorMap, which is set by the last cumulative file.
	public void readColorMap(){
		for(node tmp:nodeSet){
			tmp.color=colorMap.get(tmp.id);
			nodeSet.set(tmp.indexN, tmp);
			nodeMap.put(tmp.id, tmp);
		}
	}
	
	//compute the similarity of two ArrayList
	public double getSimilarity(ArrayList<Integer> A, ArrayList<Integer> B){
		Collections.sort(A);
		Collections.sort(B);
		//System.out.println(A);
		//System.out.println(B);
		int sameEle=0;
		int idx1=0;
		int idx2=0;
		while(idx1<A.size() && idx2<B.size()){
			int a=A.get(idx1);
			int b=B.get(idx2);
			if(a==b){
				sameEle++;
				//System.out.print(a+" ");
				idx1++;
				idx2++;
			}
			else if(a>b){
				idx2++;
			}
			else idx1++;
		}
		//System.out.println();
		double rst=sameEle/(1.0*(A.size()+B.size())/2);		
		return rst;
	}
	
	
}
