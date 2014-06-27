import java.io.*;
import java.util.*;

import org.json.*;
//import org.json.simple.*;



public class Kmeans {
	
	public ArrayList<String> fileDate=new ArrayList<String>();
	ArrayList<Integer> clusterCoordX=new ArrayList<Integer>();       //store the fixed coordinate of cluster center
	ArrayList<Integer> clusterCoordY=new ArrayList<Integer>();
	int K=4;                                                         //number of clusters
	ArrayList<String> hashtag=new ArrayList<String>();               //hashtag of nodes
	
	public static void main(String[] args)	{		
		translationSocialVis A=new translationSocialVis();
	}
	
	Kmeans(){
		solution();
	}
	
	public void solution(){
		for(int i=1;i<=31;i++){                             //the date of each json file
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
		
		
		int width=1346;                                 //set the position of clusters
		int height=647;
		int sideLen=150; 
		int centerX=width/2;
		int centerY=height/2;
		
		clusterCoordX.add(centerX-sideLen);
		clusterCoordX.add(centerX+sideLen);
		clusterCoordX.add(centerX+sideLen);
		clusterCoordX.add(centerX-sideLen);
		clusterCoordY.add(centerY-sideLen);
		clusterCoordY.add(centerY-sideLen);
		clusterCoordY.add(centerY+sideLen);
		clusterCoordY.add(centerY+sideLen);	
		
		try { 		
			String line="";                     //read the label of hashtags
			BufferedReader br=new BufferedReader(new FileReader("D:\\wamp\\www\\d3 note\\socialVis\\health_hashtag_list.csv")); 
			while ((line = br.readLine()) != null) {
				hashtag.add(line);
			} 
			br.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		int size=10;                            //number of file need to read
		
		for(int i=0;i<size;i++){
			translate(i);
			System.out.println("Read "+i+".json finished");
		}
	}	
	
	public class node{
		int indexN;                             //the rank of degree in all the nodex
		int id;
		int degree;                             //the total degree of node
		int weight;                             //the weight of node
		int group;                              //give the default group
		List<Integer> neighbor;                 //all nodes connected
		List<Integer> neiWeight;                   //the weight of edges connected to this, and the sequence is same to neighbor list
		int preGroup;                           //the last iteration's group for this node
		double [] distance=new double [K];           //the sum of distance to clusters trough each neighbor, classified by group
		node(int i,int w,int d,int g){
			id=i;
			weight=w;
			degree=d;                    
			indexN=-1;                   
			group=g;                   
			neighbor=new ArrayList<Integer>();  
			neiWeight=new ArrayList<Integer>();  
			preGroup=-1;
			}
		public boolean isStable(){             //check if the node's group is stable
			if(this.group==this.preGroup){
				this.preGroup=this.group;
				return true;
			}
			this.preGroup=this.group;
			return false;			
		}
		node(){};		
	}
	
	public class edge{
		int value;                         //the intensity of co-occurrence
		int indexE;                        //the rank of value of all edges
		node srcNode;                      //the source node
		node tarNode;                      //the target node
		edge(node a, node b,int c){
			srcNode=a;
			tarNode=b;
			value=c;
		}
		edge(){}
	}
	
	
	public void translate(int num){
		BufferedReader br = null;	
		String rst="";
		String line="";			
		try { 			                                    //read data json file
			br = new BufferedReader(new FileReader("D:\\wamp\\www\\d3 note\\socialVis\\Cooccurence\\"+num+".json")); 
			while ((line = br.readLine()) != null) {
				rst+=line;
			} 
			br.close();
			
			
			JSONObject json=new JSONObject(rst);
			JSONObject output=new JSONObject();
			
			output.put("title", json.getString("title"));               //read general property from json file
			output.put("Date", json.getString("Date"));
			output.put("Type", json.getString("Type"));
			output.put("x_axis_label", json.getString("x_axis_label"));
			output.put("y_axis_label",json.getString("y_axis_label"));
			output.put("intensity_range", json.getJSONArray("intensity_range"));
			output.put("x_axis_range", json.getJSONArray("x_axis_range"));
			output.put("y_axis_range", json.getJSONArray("y_axis_range"));
			output.put("date", fileDate.get(num));
			
			JSONArray position=json.getJSONArray("Data");              //get the x,y position json array
			JSONArray nodes=new JSONArray();                           //nodes json array
			JSONArray edges=new JSONArray();                           //edges json array
			JSONArray clusters=new JSONArray();                         //store clusters 
			
			
			
			Map<Integer,node> nodeMap=new HashMap<Integer,node>();          //node's map <id,node>
			
			for(int i=0;i<position.length();i++){                      //read position json array
				int xpos=position.getJSONObject(i).getInt("x_position");
				int ypos=position.getJSONObject(i).getInt("y_position");
				int value=position.getJSONObject(i).getInt("count");
				if(xpos!=ypos){                                        //avoid node only has relationship with itself
					if(nodeMap.containsKey(xpos)){
						node tmp=nodeMap.get(xpos);                    
						tmp.neighbor.add(ypos);
						tmp.neiWeight.add(value);
						tmp.degree++;
						tmp.weight+=value;
						nodeMap.put(xpos,tmp);
					}
					else{
						node tmp=new node(xpos,value,1,K+1);
						tmp.neighbor.add(ypos);
						tmp.neiWeight.add(value);
						nodeMap.put(xpos, tmp);
					}
				}
			}		

			
			ArrayList<node> nodeSet=new ArrayList<node>();            
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
			
			
			ArrayList<edge> edgeSet=new ArrayList<edge>();             //generate edgesSet
			for(int i=0;i<position.length();i++){                 
				int xpos=position.getJSONObject(i).getInt("x_position");
				int ypos=position.getJSONObject(i).getInt("y_position");
				if(xpos<ypos){
					int value=position.getJSONObject(i).getInt("count");
					node a=nodeMap.get(xpos);
					node b=nodeMap.get(ypos);
					edgeSet.add(new edge(a,b,value));
				}
			}     

			Comparator<edge> comparatorEdge = new Comparator<edge>() {    //define the comparator
			    public int compare(edge c1, edge c2) {
			    	if(c1.value==c2.value)
			    		return (c1.srcNode.id-c2.srcNode.id);
			    	return (c1.value-c2.value);			    	
			    }
			};
			Collections.sort(edgeSet,comparatorEdge);               //sort edge by edge's value
			
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
			
			
			for(int i=0;i<K;i++){                                 //assign K colors to the first K nodes in the sorted nodeSet
				node tmpNode=nodeSet.get(i);
				tmpNode.group=i+1;
				nodeSet.set(i, tmpNode);
				nodeMap.put(tmpNode.id, tmpNode);
				JSONObject cluster=new JSONObject();
				cluster.put("indexN", tmpNode.indexN);
				cluster.put("id", tmpNode.id);
				cluster.put("x", clusterCoordX.get(i));
				cluster.put("y", clusterCoordY.get(i));
				clusters.put(cluster);
			}			
			output.put("clusters", clusters);
			
			
			
			//K-means iteration                                                   
			int maxTimes=5000;                                    //max iteration times
			int count=0;                                          //times of iteration already executed
			int stableNum=0;                                      //number of stable nodes
			double stableCoeff=0.98;                              //stable Coefficient to estimate the stableness of nodes'group
			while(count++<maxTimes){
				stableNum=0;
				for(int i=K;i<nodeSet.size();i++){
					node cur=nodeSet.get(i);                     //cur for current checking node
					for(int j=0;j<cur.neighbor.size();j++){
						node neigh=nodeMap.get(cur.neighbor.get(j));
						if(neigh.group<=K)       //calculate the contribution of neighbor to one group, based on the distance and edge's weight
							cur.distance[neigh.group-1]+=neigh.distance[neigh.group-1]+1/Math.sqrt(cur.neiWeight.get(j));
					}
					double maxV=0;
					int maxIndex=-1;
					for(int j=0;j<cur.distance.length;j++){      //get the max Index and value of groupDegree
						if(maxV<cur.distance[j]){
							maxV=cur.distance[j];
							maxIndex=j;
						}
					}
					if(maxIndex==-1){
						cur.group=K+1;                         //no neighbor has non-default group
					}
					else cur.group=maxIndex+1;                
					if(cur.isStable())                        //if the node is stable
						stableNum++;
					//System.out.println(count+"  "+cur.id+"  "+cur.weight+"  "+cur.degree);
					nodeSet.set(i, cur);
					nodeMap.put(cur.id, cur);                 //update nodeSet and nodeMap
				}
				if(stableNum/(nodeSet.size()*1.0)>stableCoeff)
					break;
				for(node par:nodeSet){
					Arrays.fill(par.distance,0);
				}
			}
			System.out.println("count  "+count);
			
			
			
			for(int i=0;i<nodeSet.size();i++){
				JSONObject tmp=new JSONObject();
				node tmpNode=nodeSet.get(i);
				tmp.put("id",tmpNode.id);                   //store node into nodes JSONObject
				tmp.put("indexN", tmpNode.indexN);
				tmp.put("degree", tmpNode.degree);
				tmp.put("weightN", tmpNode.weight);
				tmp.put("group", tmpNode.group);
				tmp.put("label", hashtag.get(tmpNode.id));
				nodes.put(tmp);
				//System.out.println("output  "+tmpNode.id+"  "+tmpNode.weight+"  "+tmpNode.degree);
			}			
			output.put("nodes", nodes);  
			
			FileWriter fw = new FileWriter("D:\\wamp\\www\\d3 note\\socialVis\\formattedCo-Occurrence\\"+num+".json");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(output.toString());
			bw.close();
		
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
