SocialVis = function(){
	windowWidth = null; 
	windowHeight = null;
	nodeRadius = null;
	originRadius = null;
	clusterRadius = null;
	chargeForceFactor = null;
	tickSpeedFactor = null;
	transitionGapTime = null;
	gradNode = null;

	colorData = null;
	clustersData = null;
	datesData = null;

	linksData = null;
	nodesG = null;
	clustersG = null;
	linksG = null;
	cluster = null;
	node = null;
	comet = null;
	link = null;

	linkColorScale = null;
	cScale = null;
	rScale = null;
	oScale = null;
	chargeForceScale = null;
	tickSpeedScale = null;
	widthScale = null;

	svg = null;
	force = null;
	forceCluster = null;
	pos = null;
	prePosition = null;
	worldMapInstance = null;

	stopTransitFlag = null;
	stopTransitBreakIndex = null;
	initializedFlag = null;
	resetTransitionFlag = null;
	showNodePathFlag = null;
	currentDateIdx = null;
	
	pathMap = null;
	categoryMap = null;
	category = null;

	//initialize variables
	function initialize(){
		//windowWidth = window.innerWidth;
		windowWidth = parseInt($("#rightPanel").css("width"))
		windowHeight = window.innerHeight;
		originRadius = 4;									//default node's raduis
		nodeRadius = 10;									//the node's raduis
		clusterRadius = 20;									//initial radius of cluster
		chargeForceFactor = -15;   							//the charge value of force layout
		tickSpeedFactor = 0;								//factor for how faster a node move
		stopTransitFlag = true;								//if transition process need to stop
		stopTransitBreakIndex = -1;							//record the stop break point
		initializedFlag = false;							//whether or not the transition is initialized
		resetTransitionFlag = false;						//whether or not reset the transition
		currentDateIdx = 0;									//count the times of transition process
		transitionGapTime = 1500;							//time for each transition
		showNodePathFlag = true;							//show node's moving path

		//decide color of node
		cScale = d3.scale.category20();

		//decide the raduis of node
		rScale = d3.scale.linear()
			.range([1,nodeRadius]);

		//decide the opacity of node
		oScale = d3.scale.linear()
			.range([0.5, 1.2]);

		//decide the charge force of ndoe
		chargeForceScale = d3.scale.linear()
			.range([0, chargeForceFactor]);

		//return color of edge
		linkColorScale = d3.scale.linear()         
    		.range([originRadius, originRadius * 2.5]);

    	//calculate the width for node path
    	widthScale = d3.scale.linear()
    		.range([1, 10])

    	//set the speed for node when tick
    	tickSpeedScale = d3.scale.linear()
    		.domain([500, 5000])
    		.range([0.2, 0.05]);
    	tickSpeedFactor = tickSpeedScale(transitionGapTime);

		//create svg
		svg = d3.select("#rightPanel")                          							
		    .append("svg")
		    .attr("width",windowWidth)
		    .attr("height",windowHeight)
		    .attr("preserveAspectRatio", "xMidYMid")      //for map
		    .on("mousemove", mousemove);

		
		clustersG = svg.append("g");
		linksG = svg.append("g");
		nodesG = svg.append("g");

		//place to show mouse coordinate
		pos = svg.append("text")
			.attr("fill", "gold")
			.attr("font-size", 10);

		//the RGB of colors will be used to paint
		colorData = d3.range(20).map(function(d, i){  
			    return d3.rgb(cScale(i));
			});
			
		
		cluster = clustersG.selectAll(".cluster");    			//set of all clusters
		link = linksG.selectAll(".link");                		//set of all edges
		node = nodesG.selectAll(".node");                		//set of all nodes 
		comet = nodesG.selectAll(".comet");						//set of all comet

		force = d3.layout.force()                 			//create force layout
		    .friction(0.9)                           		//[0,1]  default 1
		    .gravity(0)                            			//the force to drag nodes to the enter
		    .size([windowWidth, windowHeight])
		    .on("tick",tick);

		linksData = [];										//data for all links
		nodesData = [];										//data for all nodes
		clustersData = [];									//data for clusters
		datesData = []; 									//data for dates
		pathMap = new Map();								//map for recording node's path	
		categoryMap = new Map();							//map for assign color to node by category
	}



	//tick function for nodes
	function tick(e) {
		if (resetTransitionFlag){
			return;
		}
	    var k = tickSpeedFactor * e.alpha;                      			// Push nodes toward their designated focus. 
	    node.each(function(d) {  
	    	if (d.isAppear){
		    	var diffx = clustersData[d.cluster].x - d.x;
		    	var diffy = clustersData[d.cluster].y - d.y;  
		        d.x += diffx * k;
		        d.y += diffy * k;
		        d3.select(this)	 
			        .attr("cx", function(d) { 
				    	// d.x = Math.max(d.radius, Math.min(windowWidth - d.radius, d.x)); 
				    	return d.x;
				    })
				    .attr("cy", function(d) { 
				        // d.y = Math.max(d.radius, Math.min(windowHeight - d.radius, d.y)); 
				        return d.y;
				    });  
		    } else {
		    	// d.x = clustersData[d.cluster].x;
		    	// d.y = clustersData[d.cluster].y;
		    	// d.preX = d.x;
		    	// d.preY = d.y;
		    	// d3.select(this)	 
			    //     .attr("cx", function(d) { 
				   //  	// d.x = Math.max(d.radius, Math.min(windowWidth - d.radius, d.x)); 
				   //  	return d.x;
				   //  })
				   //  .attr("cy", function(d) { 
				   //      // d.y = Math.max(d.radius, Math.min(windowHeight - d.radius, d.y)); 
				   //      return d.y;
				   //  });  
		    }   
	    }); 
	}  

	//draw component and deal with trasition process
	//index indicate which day's data is used to execute transition
	function transit(){
		//stop transition, and record break point
		var idx;
		if (currentDateIdx == datesData.length){
			stopTransitFlag = true;
			return;
		}
		else idx = currentDateIdx;

		$("#dateText").html(datesData[idx]);

		console.log("iteration: " + idx);
		setNode(idx);
		// setLink();

		force
			.nodes(nodesData)
			.charge(function(d){
		        return chargeForceScale(d.curChangeTime);
		    }) 
			.start();

		currentDateIdx += 1;

		if (!stopTransitFlag){
			setTimeout(function(){
				transit();
			}, transitionGapTime);		
		} 
		else {
			if (resetTransitionFlag){
				resetData();
			}
		}
	}

	//draw link
	function setLink(){
		link = link.data(linksData,function(d,i){        //give new loading data to edges
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
		node.each(function(d){
			if (resetTransitionFlag){
				return;
			}
			var obj = d3.select(this)
			var clusterIdx = d.appear[index];

			//add node's path
			if (index > 0 && clusterIdx != -1 && d.appear[index - 1] != -1 && clusterIdx != d.appear[index - 1]){
				var minv = Math.min(clusterIdx, d.appear[index - 1]);
				var maxv = Math.max(clusterIdx, d.appear[index - 1]);
				var key = minv + "-" + maxv;
				if (!pathMap.has(key)){
					linksG.append("line")
						.datum({
							"start" : minv,
							"end" : maxv
						})
						.attr("class", "pathLink")
						.classed("hidden", function(d){
							return !showNodePathFlag;
						})
						.attr("id", "pathLink" + key)
						.attr("x1", clustersData[minv].x)
			            .attr("y1", clustersData[minv].y)
			            .attr("x2", clustersData[maxv].x)
			            .attr("y2", clustersData[maxv].y)
			            .attr("stroke","red")
			            .attr("stroke-opacity", 0.6);
			        pathMap.set(key, 0)
				}
				pathMap.set(key, pathMap.get(key) + 1)
				linksG.selectAll("#pathLink" + key)
					.attr("stroke-width", widthScale(pathMap.get(key)));
			}
			
			//set cumulative distance
			d.distanceAry[index] = index == 0 ? 0 : d.distanceAry[index - 1];

			// 	console.log(index + " " + d.id)
			if (clusterIdx != -1 && clusterIdx != d.cluster){
				//set comet
				if (!d.isAppear){
					d.isAppear = true;
				} else {
					obj.classed("comet", true)

					//update cumulative distance
					var pre = clustersData[d.appear[index - 1]];
					var cur = clustersData[d.appear[index]];
					d.distanceAry[index] += getDistanceFromLatLonInKm(pre.lat, pre.lon, cur.lat, cur.lon);
				}

				//set radius based on change times
				d.radius = rScale(d.changeTimes[index]);
				obj.transition()
					.duration(500)
					.attr("r", d.radius);

				//set coming and leaving of cluste
				if (clusterIdx != -1 && d.cluster != -1){
					var tmpMap = clustersData[d.cluster].leaving;
					if (!tmpMap.has(clusterIdx)){
						tmpMap.set(clusterIdx, 0);
					}
					tmpMap.set(clusterIdx, tmpMap.get(clusterIdx) + 1);
					tmpMap = clustersData[clusterIdx].coming;
					if (!tmpMap.has(d.cluster)){
						tmpMap.set(d.cluster, 0);
					}
					tmpMap.set(d.cluster, tmpMap.get(d.cluster) + 1);
				}
			}
			else {				
				obj.classed("comet", false);
			}

			d.cluster = clusterIdx;
			d.curChangeTime = d.changeTimes[index];
			d.distance = d.distanceAry[index];			
			d.curPostCount = d.appearTimes[index];
			obj.attr("opacity", oScale(d.curPostCount));
			// console.log("cluster idx " + d.cluster)
		})
	}

	//initialize nodes
	function initializeNodes(){
		node = node.data(nodesData, function(d){
			return d.id;
		});
		node.enter()
			.append("circle")
	        .attr("class", "node")
	        // .attr("class", "comet")
	        .attr("id", function(d){
	        	return "node" + d.id;
	        })
	        .attr("fill", function(d, i){
	        	// d.categoryType = setNodeCategory(category, d.category);
	        	d.cluster = -1;
	        	d.curPostCount = 0;
	        	d.distanceAry = [];
	        	d.distance = 0;
	        	// d.changeTimes = [];
	        	d.isAppear = false;
	        	return cScale(d.color);
	            // return "url(#grad" + d.color + ")";
	        })
	        .attr("r", 0)
	        .attr("cx", function(d){
	        	d.x = clustersData[d.color].x;
	        	return d.x;
	        })
	        .attr("cy", function(d){
	        	d.y = clustersData[d.color].y;
	        	return d.y;
	        })
	        .on("mouseover", function(d){
	        	ary = d3.mouse(this);
			 	d3.select("#nodeToolTip")               //set the tool tip for nodes    
			        .style("left", (ary[0] + (window.innerWidth - windowWidth) + 10) + "px")              
			        .style("top",(ary[1] + 10) + "px")
			        .classed("hidden", false)
			        .moveToFront();         
			    var content = "Id: " + d.id + 
			    	"<br>City: " + clustersData[d.cluster].city + 
			    	"<br>Phone: " + d.label + 
			    	"<br>postTimes: " + d.curPostCount + 
			    	"<br>Distance: " + Math.ceil(d.distance) + 
			    	"<br>category: " + d.category;

			    $("#nodeToolTip").html(content);
			    highlightNodePath(d.id);
			})
			.on("mouseout", function(d){
				d3.select("#nodeToolTip")               //hide the tool tip for nodes 
			        .classed("hidden", true)
			        .moveToBack();
			    quithighlightNodePath(d.id)
			});
	}

	//draw clusters
	function initializeClusters(){
		cluster = cluster.data(clustersData, function(d){
			return d.id;
		});
		cluster.enter()
			.append("circle")
			.attr("class","cluster")
			.attr("id", function(d){
				return "cluster" + d.id;
			})
	        .attr("fill", "transparent")
	        .attr("stroke", function(d, i){
		    	d.coming = new Map();
		    	d.leaving = new Map();
	            return cScale(d.group);
	        })
	        .attr("r", clusterRadius)
	        .attr("stroke-width", 2)   
	        .attr("stroke-opacity", 0)
		    .on("mouseover", function(d){
			 	//set the tool tip for nodes 
	            ary = d3.mouse(this);   
			    d3.select("#clusterToolTip")
			    	.style("left", (ary[0] + (window.innerWidth - windowWidth) + 10) + "px")              
			        .style("top",(ary[1] + 10) + "px")
			        .classed("hidden", false)
			        .moveToFront();  
			    var content = "Id: " + d.id + "<br>State: " + d.state + "<br>City: " + d.city;
			    $("#clusterToolTip").html(content);
			    d3.select(this)
			    	.attr("stroke-opacity", 0.8)

				//hightlight coming and leaving path
				d.coming.forEach(function(value, key){
					linksG.append("line")
						.attr("class", "clusterPath")
						.attr("stroke-width", 2)
			            .attr("stroke", "yellow")
			            .attr("stroke-opacity", 0.8)
			            .attr("x1", clustersData[key].x)
			            .attr("y1", clustersData[key].y)
			            .attr("x2", d.x)
			            .attr("y2", d.y)
				});
				d.leaving.forEach(function(value, key){
					linksG.append("line")
						.attr("class", "clusterPath")
						.attr("stroke-width", 2)
			            .attr("stroke", "blue")
			            .attr("stroke-opacity", 0.8)
			            .attr("x1", clustersData[key].x + 2)
			            .attr("y1", clustersData[key].y + 2)
			            .attr("x2", d.x + 2)
			            .attr("y2", d.y + 2)
				});
		        // linksG.moveToFront();
	        }) 
	        .on("mouseout", function(d){
	        	//hide the tool tip for nodes
	          	d3.select("#clusterToolTip")                
			        .classed("hidden", true)
			        .moveToBack();
			    d3.select(this)
			    	.attr("stroke-opacity", 0)

			    //delete coming and leaving highlighted path
			    d3.selectAll(".clusterPath")
					.remove();
	        })
	        //.call(forceCluster.drag);
	    cluster.exit()
	    	.transition()
	    	.duration(500)
	    	.attr("opacity", 0)
	    	.remove();

	    updateClusters(1);
	}

	//update the coordinate of cluster due to the operation of zoom
	function updateClusters(zoomScale){
		//set the coordinate of cluster, get from world map's function getClusterCoordinates
		clustersData.forEach(function(d){
	    	var coord = worldMapInstance.getClusterCoordinates({
	    		"lat" : d.lat,
	    		"lon" : d.lon
	    	});
	    	d.x = coord[0];
	    	d.y = coord[1];
	    })

	   cluster
	    	.attr("cx", function(d){
	    		return d.x;
	    	})
	    	.attr("cy", function(d){
	    		return d.y;
	    	});

	    force.start();
	    updateNodePath();
	    // clustersG.moveToFront();
	    // nodesG.moveToFront();
	}

	//update node's path
	function updateNodePath(){
		linksG.selectAll(".pathLink")
			.attr("x1", function(d){
				return clustersData[d.start].x;
			})
			.attr("y1", function(d){
				return clustersData[d.start].y;
			})
			.attr("x2", function(d){
				return clustersData[d.end].x;
			})
			.attr("y2", function(d){
				return clustersData[d.end].y;
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

	//Calculate distance of two geo locations
	function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
	  	var R = 6371; // Radius of the earth in km
	  	var dLat = (Math.PI/180) * (lat2-lat1); 
	  	var dLon = (Math.PI/180) * (lon2-lon1); 
	  	var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos((Math.PI/180) * (lat1)) * Math.cos((Math.PI/180) * (lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2); 
	  	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
	  	var d = R * c; // Distance in km
	  	return d;
	}

	//add tail when node moves, the less the second parameter of timer, the smoother the tail
	d3.timer(function(){
	    nodesG.selectAll(".comet")
	        .each(function(d, i){
	            if (!d.preX){
	                d.preX = d.x;
	                d.preY = d.y;
	            }
	            svg.append("line")
	            	.attr("class", "cometTrail")
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
	}, 350);


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

	//reset variables for new query.
	function resetTransit(){
		if (!initializedFlag){
			return;
		}
		console.log("Reset transition");
		if (stopTransitFlag){
			resetData();
		} else {
			stopTransitFlag = true;
			resetTransitionFlag = true;
		}
	}

	//reset data for next query
	function resetData(){
		force.stop();
		node.transition()
			.duration(300)
			.attr("opacity", 0)
			.remove();
		node = node.data([]);
		cluster.remove();
		cluster = cluster.data([]);
		initializedFlag = false;
		resetTransitionFlag = false;
		console.log("Reset data");
	}

	//stop transition
	function stopTransit(){
		if (stopTransitFlag){
			alert("Not started socialVis yet!");
			return;
		}
		stopTransitFlag = true;
	}

	//restart transition
	function restartTransit(){
		if (stopTransitFlag){
			stopTransitFlag = false;
			console.log("Resume transition")
			transit();
		}
	} 

	//highlight the chosen node's path, and reduce opacity of other parts
	//also stop transition
	function highlightNodePath(id){
		var nodeId = id;
		// console.log("Highlight node path " + nodeId);
		//retrieve node's path from former data
		var historyText = "";
		var edIdx = Math.min(currentDateIdx, datesData.length - 1);
		var pre = -1;
		var dashgapCount = 2;
		for (var i = 0; i <= edIdx; i++){
			var cltIdx = nodesData[nodeId].appear[i];
			if(pre == -1 && cltIdx != -1){
				historyText += "<span>" + datesData[i] + " : " + clustersData[cltIdx].city + "</span><br>";
				pre = cltIdx;
			}
			else if (pre != -1 && cltIdx != -1 && cltIdx != pre){
				historyText += "<span>" + datesData[i] + " : to " + clustersData[cltIdx].city + "</span><br>";
				linksG.append("line")
					.attr("class", "nodePath")
					.attr("stroke-width", 2)
					.attr("stroke-dasharray", dashgapCount + " " + 4)
		            .attr("stroke", cScale(nodesData[nodeId].color))
		            .attr("stroke-opacity", 0.8)
		            .attr("x1", clustersData[pre].x)
		            .attr("y1", clustersData[pre].y)
		            .attr("x2", clustersData[cltIdx].x)
		            .attr("y2", clustersData[cltIdx].y)
		        dashgapCount += 2;
		        // linksG.moveToFront();
				pre = cltIdx;
			}
		}
		$("#nodeHistory").html(historyText);
	}

	//quit highlight node path and resume transition
	function quithighlightNodePath(id){
		$("#nodeHistory").html("");
		// console.log("Delete highlight node path " + id);

		d3.selectAll(".nodePath")
			// .attr("opacity", 0)
			.remove();
	}

	//jump from current date to specific date
	function jumpTransit(val){
		var dateIdx = parseInt(val);
		if (dateIdx < 0 || dateIdx >= datesData.length){
			console.log("Invalid jump date");
			return;
		}
		resetTransit();
		setTimeout(function(d){
			initializeClusters();
			updateClusters(1);
			initializeNodes();
			currentDateIdx = dateIdx;
			setTimeout(function(){
				stopTransitFlag = false;	
				initializedFlag = true;	
				transit();
			}, 1000);
		}, transitionGapTime);
	}

	//whether or not show the node's moving path
	function setShowNodePath(val){
		showNodePathFlag = val;
		if (val){
			linksG.selectAll(".pathLink")
				.classed("hidden", false);
		} else {
			linksG.selectAll(".pathLink")
				.classed("hidden", true);
		}
	}

	//execute once the document loaded
	$(document).ready(function() {
		initialize();
		createMap();
		worldMapInstance.generateMap();		
	});

	//start generate social vis layout
	function generateLayout(data){
		clustersData = data.clusters;
		nodesData = data.nodes;
		datesData = data.dates;
		category = data.category;
		categoryMap.clear();
		initializeClusters();
		updateClusters(1);
		initializeNodes();
		// oScale.domain([1, Math.ceil(Math.log(datesData.length))]);
		oScale.domain([1, datesData.length * 3]);
		rScale.domain([0, datesData.length / 2 + 1]);
		widthScale.domain([1, datesData.length / 4 + 1]);
		chargeForceScale.domain([0, datesData.length / 2 + 1]);
		currentDateIdx = 0;

		setTimeout(function(){
			pos.moveToFront();	
			stopTransitFlag = false;	
			initializedFlag = true;	
			transit();
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
		updateClusters(zoomScale);
	}

	//useless
	this.showNodePath = function(text){
		highlightNodePath(text);
	}

	//useless
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

	this.resetTransition = function(){
		resetTransit();
	}

	this.setTransitionGap = function(val){
		console.log("Set transition gap time : " + val);
		transitionGapTime = val;
		if (val < 5000){
			tickSpeedFactor = 0.6;
		} else {
			tickSpeedFactor = tickSpeedScale(val);
		}
	}

	this.setShowNodePathAPI = function(val){
		setShowNodePath(val);
	}
};
