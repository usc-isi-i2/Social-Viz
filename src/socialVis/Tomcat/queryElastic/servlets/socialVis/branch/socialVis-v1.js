SocialVis = function(){
	windowWidth = null; 
	windowHeight = null;
	nodeRadius = null;
	originRadius = null;
	clusterRadius = null;
	cometRemoveThreshold = null;
	chargeForceFactor = null;
	cScale = null;
	gradNode = null;
	colorData = null;
	clusterData = null;
	dailyData = null;
	linkColorScale = null;
	svg = null;
	force = null;
	forceCluster = null;
	cluster = null;
	node = null;
	pos = null;
	prePosition = null;
	worldMapInstance = null;
	clusterMap = null;
	stopTransitFlag = null;
	stopTransitBreakIndex = null;
	transitionTimesCounter = null;
	

	//initialize variables
	function initialize(){
		//windowWidth = window.innerWidth;
		windowWidth = parseInt($("#rightPanel").css("width"))
		windowHeight = window.innerHeight;
		originRadius = 3;									//default node's raduis
		nodeRadius = originRadius;							//the node's raduis
		clusterRadius = 20;									//initial radius of cluster
		cometRemoveThreshold = 5;                         	//then the distance of comet and its distination less than threshold, remove comet effect
		chargeForceFactor = -5;   							//the charge value of force layout
		stopTransitFlag = false;							//if transition process need to stop
		stopTransitBreakIndex = -1;							//record the stop break point
		transitionTimesCounter = 0;							//count the times of transition process

		//decide color of node
		cScale = d3.scale.category20();

		//return color of edge
		linkColorScale = d3.scale.linear()         
    		.range([0, 1]);

		//create svg
		svg = d3.select("#rightPanel")                          							
		    .append("svg")
		    .attr("width",windowWidth)
		    .attr("height",windowHeight)
		    .attr("preserveAspectRatio", "xMidYMid")      //for map
		    .on("mousemove", mousemove);

		//place to show mouse coordinate
		pos = svg.append("text")
			.attr("fill", "gold")
			.attr("font-size", 10);

		//the RGB of colors will be used to paint
		colorData = d3.range(20).map(function(d, i){  
			    return d3.rgb(cScale(i));
			});

		//set the gradient parameter for svg node
		gradNode = svg.append("defs")
		    .selectAll("radialGradient")
		    .data(colorData)
		    .enter()
		    .append("radialGradient")
		    .attr("id", function(d, i){
		        return "grad" + i;
		    })
		    .attr("cx", "50%")
		    .attr("cy", "50%")
		    .attr("r", "70%");
		//Radial gradients use to fill nodes, give a feeling of comet              
		gradNode.append("stop")
		    .attr("offset", "0%")
		    .style("stop-color", function(d){
		        //var color = d3.rgb(cScale(d.color));
		        return "rgb(" + d.r + "," + d.g + "," + d.b + ")";
		    })
		    .attr("stop-opacity", 1);
		gradNode.append("stop")
		    .attr("offset", "20%")
		    .style("stop-color", function(d){
		        return "rgb(" + Math.floor(d.r / 2) + "," + Math.floor(d.g / 2) + "," + Math.floor(d.b / 2) + ")";
		    })
		    .attr("stop-opacity", 0.7);
		gradNode.append("stop")
		    .attr("offset", "100%")
		    .style("stop-color", function(d){
		        return "rgb(0, 0, 0)";
		    })
		    .attr("stop-opacity", 0);
			
		
		cluster = svg.selectAll(".cluster");    			//set of all clusters
		link = svg.selectAll(".link");                		//set of all edges
		node = svg.selectAll(".node");                		//set of all nodes 

		force = d3.layout.force()                 			//create force layout
		    .charge(chargeForceFactor)     
		    .friction(0.9)                           		//[0,1]  default 1
		    .gravity(0)                            			//the force to drag nodes to the enter
		    .size([windowWidth, windowHeight])
		    .on("tick",tick);
		linkData = [];										//data for all links
		nodeData = [];										//data for all nodes
		prePosition = new Map();							//record position of node in previous transition
		clusterMap = new Map();								//key of map is cluster id, values is the object
	}

	//draw component and deal with trasition process
	//index indicate which day's data is used to execute transition
	function transit(){
		//stop transition, and record break point
		if (transitionTimesCounter == dailyData.length){
			stopTransitFlag = true;
			return;
		}

		var date = dailyData[transitionTimesCounter].Date
		$("#dateText").html(date);

		console.log("iteration: " + transitionTimesCounter);
		setNode(transitionTimesCounter);
		// setLink();

		force.nodes(nodeData)
			//.links(linkData)
			.start();

		/*forceCluster.nodes(clusterData)
			.start();*/

		transitionTimesCounter += 1;

		if (!stopTransitFlag){
			setTimeout(function(){
				transit();
			}, 5000);		
		}
	}

	//record node position for next iteration
	function recordNodePostion(){
		prePosition.clear();
		nodeData.forEach(function(d){
			prePosition.set(d.id, {
				x : d.x,
				y : d.y,
				cluster : d.cluster
			})
		})
		// var mapIter = prePosition.entries();
		// var count = 0;
		// while (count < prePosition.size){
		// 	var tmp = mapIter.next().value;
		// 	count++;
		// 	console.log(tmp[0] + " " + tmp[1].x + " " + tmp[1].y + " " + tmp[1].cluster);
		// }
	}

	//draw link
	function setLink(){
		link = link.data(linkData,function(d,i){        //give new loading data to edges
	        return d.source.id + "-" + d.target.id;
	    });

	    link.enter()                                  //for new added edges, assign attribute
	        .append("line")
	        .attr("class", "link")
	        .attr("stroke", function(d){
	            var fraction = linkColorScale(Math.ceil(d.nodeWeight));
	            return d3.hsl((fraction * 360) ,1 , 0.5);
	        })
	   
	    link.exit()                                   //remove edges are not appear in json file
	        .transition() 
	        .duration(200)
	        .attr("stroke-opacity", 0)
	        .remove();

	    link.moveToBack();
	}

	//draw nodes
	function setNode(index){
		nodeData = dailyData[index].nodes;

		node = node.data(nodeData,function(d){            	//give new loading data to nodes
	        return d.id;    
	    });
	    node.enter()                                       	//for new added node assign attributes
	        .append("circle")
	        .attr("class", "node")
	        // .classed("comet", false)
	        .classed("node", true)
	        .attr("fill", function(d, i){
	        	return cScale(d.color);
	            //return "url(#grad" + d.color + ")";
	        })
	        .attr("r", function(d){
	            return nodeRadius;
	        })
	        .call(force.drag)
	        .on("mouseover", function(d){
	        	//console.log("mouseover")
	        	ary = d3.mouse(this);
			 	d3.select("#nodeToolTip")               //set the tool tip for nodes    
			        .style("left", (ary[0] + 10) + "px")              
			        .style("top",(ary[1] + 10) + "px")
			        .moveToFront();         
			    d3.select("#term1")
			        .text(d.id);
			    d3.select("#term2")
			        .text(clusterMap.get(d.cluster).label);
			    d3.select("#term3")
			        .text(d.label);
			    d3.select("#nodeToolTip")
			        .classed("hidden", false);

			    highlightNodePath(d.id);
			})
			.on("mouseout", function(d){
				//console.log("mouseout")
				d3.select("#nodeToolTip")               //hide the tool tip for nodes 
			        .classed("hidden", true)
			        .moveToBack();

			    quithighlightNodePath(d.id)
			});

	    node.each(function(d){
	    	d.radius = nodeRadius;
	    	d.isComet = false;
	    	if (prePosition.has(d.id)){
	    		d.x = prePosition.get(d.id).x;
	    		d.y = prePosition.get(d.id).y;
	    		// console.log(d.cluster + " " + prePosition.get(d.id).cluster)
	    		if (d.cluster != prePosition.get(d.id).cluster){
	    			d3.select(this)
	        			.classed("comet", true);
	        		d.isComet = true;
	    		}
	    	} 
	    	else {	    		
	        	d.x = clusterMap.get(d.cluster).x + Math.floor((Math.random() - 0.5) * 40);
	        	d.y = clusterMap.get(d.cluster).y + Math.floor((Math.random() - 0.5) * 40);
	    	}
	    	d3.select(this)
	    		.attr("opacity", 1)
	    });

	    node.transition()                              		//set transition process, the radius of node from 0 to 5 
	        .duration(300)
	        .attr("r", function(d){
	        	return d.radius;
	        });

	    node.exit()                                        	//remove nodes no longer in the json file
	        .transition()
	        .duration(500)
	        .attr("opacity", 0)
	        .remove();		
	}


	//draw clusters
	function initializeCluster(){
		cluster = cluster.data(clusterData, function(d){
			return d.id;
		});
		cluster.enter()
			.append("circle")
			.attr("class","cluster")
	        .attr("fill", "transparent")
	        .attr("stroke", function(d, i){
	            return cScale(d.id);
	        })
	        .attr("r", clusterRadius)
	        .attr("stroke-width", 2)   
	        .attr("stroke-opacity", 0)
		    .on("mouseover", function(d){
	            ary = d3.mouse(this);
			 	d3.select("#nodeToolTip")               //set the tool tip for nodes    
			        .style("left", (ary[0] + 10) + "px")              
			        .style("top",(ary[1] + 10) + "px")
			        .moveToFront();         
			    d3.select("#term1")
			        .text(d.id);
			    d3.select("#term2")
			        .text(d.group);
			    d3.select("#term3")
			        .text(d.label);
			    d3.select("#nodeToolTip")
			        .classed("hidden", false);

			    d3.select(this)
			    	.attr("stroke-opacity", 0.8)
	        }) 
	        .on("mouseout", function(d){
	          	d3.select("#nodeToolTip")               //hide the tool tip for nodes 
			        .classed("hidden", true)
			        .moveToBack();

			    d3.select(this)
			    	.attr("stroke-opacity", 0)
	        })
	        //.call(forceCluster.drag);
	    cluster.exit()
	    	.transition()
	    	.duration(500)
	    	.attr("opacity", 0)
	    	.remove();

	    updateCluster(1);
	}

	//update the coordinate of cluster due to the operation of zoom
	function updateCluster(zoomScale){
		//set the coordinate of cluster, get from world map's function getClusterCoordinates
		if (clusterData == null)
			return;
	    clusterData.forEach(function(d){
	    	var coord = worldMapInstance.getClusterCoordinates({
	    		"lat" : d.lat,
	    		"long" : d.long
	    	});
	    	d.x = coord[0];
	    	d.y = coord[1];
	    })

	    cluster.each(function(d){
	    	d3.select(this)
	    		.attr("cx", d.x)
	    		.attr("cy", d.y)
	    })

	    //force.charge(zoomScale * chargeForceFactor)
	    force.start();
	    cluster.moveToFront();
	    node.moveToFront();
	    
	    if (zoomScale <= 1)
	    	nodeRadius = originRadius;
	    else if (zoomScale < 4)
	    	nodeRadius = originRadius * Math.sqrt(zoomScale)
	    else nodeRadius = originRadius * 2
	    // console.log(nodeRadius)
	}

	//tick function for nodes
	function tick(e) {
	    var k = .08 * e.alpha;                      			// Push nodes toward their designated focus. 
	    node.each(function(d, i) {  
	    	var diffx = clusterMap.get(d.cluster).x - d.x;
	    	var diffy = clusterMap.get(d.cluster).y - d.y;  
	        var distance = Math.sqrt(diffx * diffx + diffy * diffy);
	        if (d.isComet && distance < cometRemoveThreshold){
	        	d3.select(this)
	        		.classed("comet", false);
	        	d.isComet = false;
	        }
	        d.x += diffx * k;
	        d.y += diffy * k;	       
	    }); 

	    link.attr("x1", function(d) { return d.source.x; })
	        .attr("y1", function(d) { return d.source.y; })
	        .attr("x2", function(d) { return d.target.x; })
	        .attr("y2", function(d) { return d.target.y; });

	    node.attr("cx", function(d) { 
	    		d.x = Math.max(d.radius, Math.min(windowWidth - d.radius, d.x)); 
	    		return d.x;
	    	})
	        .attr("cy", function(d) { 
	        	d.y = Math.max(d.radius, Math.min(windowHeight - d.radius, d.y)); 
	        	return d.y;
	        });
	}   

	//move element to the back of its parent's children
	d3.selection.prototype.moveToBack = function() { 
	    return this.each(function() { 
	        var firstChild = this.parentNode.firstChild; 
	        if (firstChild) { 
	            this.parentNode.insertBefore(this, firstChild); 
	        } 
	    });    //move component to the down of svg
	};

	//move element to the top of its parent's children
	d3.selection.prototype.moveToFront = function() {
	  	return this.each(function(){
	    	this.parentNode.appendChild(this);
	  	});   //move component to the up of svg
	};

	

	//add tail when node moves, the less the second parameter of timer, the smoother the tail
	d3.timer(function(){
	    d3.selectAll(".comet")
	        .each(function(d, i){
	            if (!d.preX){
	                d.preX = d.x;
	                d.preY = d.y;
	            }
	            svg.append("line")
	                .attr("stroke-width", 2)
	                .attr("stroke", cScale(d.color))
	                .attr("stroke-opacity", 1)
	                .attr("x1", d.preX)
	                .attr("y1", d.preY)
	                .attr("x2", d.x)
	                .attr("y2", function(){
	                    d.preX = d.x;
	                    d.preY = d.y;
	                    return d.y;
	                })
	                .transition()
	                .duration(700)
	                .attr("stroke-width", 0)
	                .remove();
	        })
	}, 500);


	//create wolrdMap class and draw map
	function createMap(){
		worldMapInstance = new WorldMap(svg);
	}

	//when move over show the coordinate
	function mousemove(){
		var ary = d3.mouse(this);
		pos//.attr("x", ary[0] + 10)
			//.attr("y", ary[1] + 10)
			.attr("x", 20)
			.attr("y", 20)
			.text(Math.round(ary[0]) + ", " + Math.round(ary[1]))
	}

	//stop transition
	function stopTransit(){
		if (dailyData == null){
			alert("Not started socialVis yet!");
			return;
		}
		stopTransitFlag = true;
		stopTransitBreakIndex = transitionTimesCounter;
	}

	//restart transition
	function restartTransit(){
		var tmpFlag = stopTransitFlag;
		stopTransitFlag = false;
		//recordNodePostion();
		if (tmpFlag){
			console.log("Resume transition")
			transitionTimesCounter = stopTransitBreakIndex;
			transit();
		}
	}

	//highlight the chosen node's path, and reduce opacity of other parts
	//also stop transition
	function highlightNodePath(text){
		var index = -1;
		if (text.length > 10){
			index = 0;
			while (index < nodeData.length){
				if (nodeData[index].label == text){
					break;
				}
				index++;
			}
			if (index == nodeData.length){
				alert("invalid input");
				return;
			}			
		} else {
			try {
				index = parseInt(text);
			} catch (e){
				alert("invalid input");
				return;
			}
		}
		if (index < 0){
			alert("invalid input");
			return;
		}
		var nodeId = index;
		// console.log(nodeId);
		// if (!stopTransitFlag)
		// 	stopTransit();
		d3.selectAll(".node")
			.transition()
			.duration(500)
			.attr("opacity", 0.3);

		//retrieve node's path from former data
		var historyText = "";
		index = 0;
		var pre = -1;
		
		tmpTransitionIdx = transitionTimesCounter;
		while (index < tmpTransitionIdx){
			dailyData[index].nodes.forEach(function(d){
				var date = dailyData[index].Date;
				if (d.id == nodeId){
					var clusterId = d.cluster;
					if (pre != clusterId){
						if (pre == -1)
							historyText += "<span>" + date + " : " + "Start at " + clusterMap.get(clusterId).label + "</span><br>";
						else
							historyText += "<span>" + date + " : " + clusterMap.get(pre).label + " to " + clusterMap.get(clusterId).label + "</span><br>";
					}

					if (pre > -1 && pre != clusterId){
						svg.append("line")
							.classed("nodePath", true)
							.attr("stroke-width", 2)
			                .attr("stroke", cScale(d.color))
			                .attr("stroke-opacity", 0.8)
			                .attr("x1", clusterMap.get(pre).x)
			                .attr("y1", clusterMap.get(pre).y)
			                .attr("x2", clusterMap.get(clusterId).x)
			                .attr("y2", clusterMap.get(clusterId).y)
			            //console.log(clusterMap.get(pre).x + " " + clusterMap.get(pre).y + " " + clusterMap.get(clusterId).x + " " + clusterMap.get(clusterId).y)
					}

					pre = clusterId;
				}
			})
			index++;
		}
		$("#nodeHistory").html(historyText);
		// console.log(historyText);
	}

	//quit highlight node path and resume transition
	function quithighlightNodePath(){
		d3.selectAll(".node")
			.attr("opacity", 1);
		// if (stopTransitFlag)
		// 	restartTransit();
		$("#nodeHistory").html("");
		svg.selectAll(".nodePath")
			.transition()
			.duration(500)
			.attr("opacity", 0)
			.remove();
	}

	//jump from current date to specific date
	function jumpTransit(val){
		transitionTimesCounter = parseInt(val);
		stopTransitBreakIndex = transitionTimesCounter;
		if (stopTransitFlag){
			console.log("Restart transition");
			restartTransit();
		}
		// stopTransit();
		// node.remove();
		// prePosition.clear();
		// setTimeout(function(){
		// 	stopTransitBreakIndex = parseInt(val);
		// 	restartTransit();
		// }, 2000)
	}

	//execute once the document loaded
	$(document).ready(function() {
		initialize();
		createMap();
		worldMapInstance.generateMap();		
	});

	//start generate social vis layout
	function generateLayout(data){
		clusterData = data.clusters;
		// if (clusterMap == null)
		// 	clusterMap = new Map();
		clusterData.forEach(function(d){
			clusterMap.set(d.id, d);
		})
		// console.log("Cluster map's size is " + clusterMap.size);

		dailyData = data.dailyData;

		setTimeout(function(){
			pos.moveToFront();
			initializeCluster();
			transit();
			updateCluster(1);
		}, 1000);
	}


	//******************** public method below *************************
	this.generateSocialVis = function(path){
		//lode data from input json
		d3.json(path, function(data){		    	
		    console.log(path + " loaded");
		    generateLayout(data);
		});
	}

	this.generateSocialVisFromServlet = function(data){
		generateLayout(data);
	}

	this.mapScaleChange = function(zoomScale){
		updateCluster(zoomScale);
	}

	this.showNodePath = function(text){
		highlightNodePath(text);
	}

	this.quitShowPath = function(){
		quithighlightNodePath();
	}

	this.stopTransition = function(){
		stopTransit();
	}

	this.resumeTransition = function(){
		restartTransit();
	}

	this.jumpTransition = function(val){
		jumpTransit(val);
	}
};
