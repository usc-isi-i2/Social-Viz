SocialVis = function(){
	windowWidth = null; 
	windowHeight = null;
	nodeRadius = null;
	originRadius = null;
	clusterRadius = null;
	cometRemoveThreshold = null;
	chargeForceFactor = null;
	transitionGapTime = null;
	gradNode = null;

	colorData = null;
	clustersData = null;
	datesData = null;
	forceNodesData = null;

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
	chargeForceScale = null;

	svg = null;
	force = null;
	forceCluster = null;
	pos = null;
	prePosition = null;
	worldMapInstance = null;

	stopTransitFlag = null;
	stopTransitBreakIndex = null;
	currentDateIdx = null;
	

	//initialize variables
	function initialize(){
		//windowWidth = window.innerWidth;
		windowWidth = parseInt($("#rightPanel").css("width"))
		windowHeight = window.innerHeight;
		originRadius = 4;									//default node's raduis
		nodeRadius = 10;									//the node's raduis
		clusterRadius = 20;									//initial radius of cluster
		cometRemoveThreshold = 5;                         	//then the distance of comet and its distination less than threshold, remove comet effect
		chargeForceFactor = -15;   							//the charge value of force layout
		stopTransitFlag = true;								//if transition process need to stop
		stopTransitBreakIndex = -1;							//record the stop break point
		currentDateIdx = 0;									//count the times of transition process
		transitionGapTime = 1500;							//time for each transition

		//decide color of node
		cScale = d3.scale.category20();

		//decide the raduis of node
		rScale = d3.scale.linear()
			.range([1,nodeRadius]);

		//decide the charge force of ndoe
		chargeForceScale = d3.scale.linear()
			.range([0, chargeForceFactor]);

		//return color of edge
		linkColorScale = d3.scale.linear()         
    		.range([originRadius, originRadius * 2.5]);

		//create svg
		svg = d3.select("#rightPanel")                          							
		    .append("svg")
		    .attr("width",windowWidth)
		    .attr("height",windowHeight)
		    .attr("preserveAspectRatio", "xMidYMid")      //for map
		    .on("mousemove", mousemove);

		nodesG = svg.append("g");
		clustersG = svg.append("g");
		linksG = svg.append("g");

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


		linksData = [];										//data for all links
		nodesData = [];										//data for all nodes
		clustersData = [];									//data for clusters
		datesData = []; 									//data for dates
		forceNodesData = [];								//data for nodes change their info


		force = d3.layout.force()                 			//create force layout
		    .friction(0.9)                           		//[0,1]  default 1
		    .gravity(0)                            			//the force to drag nodes to the enter
		    .size([windowWidth, windowHeight])
		    .on("tick",tick);
	}


	//tick function for nodes
	function tick(e) {
	    var k = .08 * e.alpha;                      			// Push nodes toward their designated focus. 
	    node.each(function(d) {  
	    	if (d.isAppear){
		    	var diffx = clustersData[d.cluster].x - d.x;
		    	var diffy = clustersData[d.cluster].y - d.y;  
		        // var distance = Math.sqrt(diffx * diffx + diffy * diffy);
		        // if (d.isComet && distance < cometRemoveThreshold){
		        // 	d3.select(this)
		        // 		.classed("comet", false);
		        // 	d.isComet = false;
		        // }
		        d.x += diffx * k;
		        d.y += diffy * k;
		        d3.select(this)	 
			        .attr("cx", function(d) { 
				    	d.x = Math.max(d.radius, Math.min(windowWidth - d.radius, d.x)); 
				    	return d.x;
				    })
				    .attr("cy", function(d) { 
				        d.y = Math.max(d.radius, Math.min(windowHeight - d.radius, d.y)); 
				        return d.y;
				    });  
		    }    
	    }); 
	}  

	//draw component and deal with trasition process
	//index indicate which day's data is used to execute transition
	function transit(){
		// if (currentDateIdx >= 5)
		// 	return;
		//stop transition, and record break point
		var idx;
		if (currentDateIdx == datesData.length || stopTransitFlag){
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
			var obj = d3.select(this)
			var clusterIdx = d.appear[index];
			if (clusterIdx != -1 && clusterIdx != d.cluster){
				d.cluster = clusterIdx;
				d.changeTimes[index] = ((index == 0) ? 0 : d.changeTimes[index - 1]) + 1;

				if (!d.isAppear){
					d.isAppear = true;
				} else {
					obj.classed("comet", true)
				}
				d.radius = rScale(d.changeTimes[index]);
				obj.transition()
					.duration(500)
					.attr("r", d.radius);
			}
			else {				
				obj.classed("comet", false);
				d.changeTimes[index] = (index == 0) ? 0 : d.changeTimes[index - 1];
			}
			d.curChangeTime = d.changeTimes[index];
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
	        	d.cluster = -1;
	        	d.changeTimes = [];
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
			        .style("left", (ary[0] + 10) + "px")              
			        .style("top",(ary[1] + 10) + "px")
			        .classed("hidden", false)
			        .moveToFront();         
			    var content = "Id: " + d.id + "<br>City: " + clustersData[d.cluster].city + "<br>Phone: " + d.label;
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
	        .attr("fill", "transparent")
	        .attr("stroke", function(d, i){
	            return cScale(d.group);
	        })
	        .attr("r", clusterRadius)
	        .attr("stroke-width", 2)   
	        .attr("stroke-opacity", 0)
		    .on("mouseover", function(d){
		    	d.coming = [];
		    	d.leaving = [];

	            ary = d3.mouse(this);
			 	//set the tool tip for nodes    
			    d3.select("#clusterToolTip")
			    	.style("left", (ary[0] + 10) + "px")              
			        .style("top",(ary[1] + 10) + "px")
			        .classed("hidden", false)
			        .moveToFront();  
			    var content = "Id: " + d.id + "<br>State: " + d.state + "<br>City: " + d.city;
			    $("#clusterToolTip").html(content);

			    d3.select(this)
			    	.attr("stroke-opacity", 0.8)
	        }) 
	        .on("mouseout", function(d){
	          	d3.select("#clusterToolTip")               //hide the tool tip for nodes 
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
	    clustersG.moveToFront();
	    nodesG.moveToFront();
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
	}, 200);


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
		console.log("Highlight node path " + nodeId);
		//retrieve node's path from former data
		var historyText = "";
		var edIdx = Math.min(currentDateIdx, datesData.length - 1);
		var pre = -1;
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
					.attr("class", "line")
					.attr("stroke-width", 2)
		            .attr("stroke", cScale(nodesData[nodeId].color))
		            .attr("stroke-opacity", 0.8)
		            .attr("x1", clustersData[pre].x)
		            .attr("y1", clustersData[pre].y)
		            .attr("x2", clustersData[cltIdx].x)
		            .attr("y2", clustersData[cltIdx].y)
		        linksG.moveToFront();
				pre = cltIdx;
			}
		}
		$("#nodeHistory").html(historyText);
	}

	//quit highlight node path and resume transition
	function quithighlightNodePath(id){
		$("#nodeHistory").html("");
		console.log("Delete highlight node path " + id);

		d3.selectAll("line")
			.attr("opacity", 0)
			.remove();
	}

	//jump from current date to specific date
	function jumpTransit(val){
		var dateIdx = parseInt(val);
		if (dateIdx < 0 || dateIdx >= datesData.length){
			console.log("Invalid jump date");
			return;
		}
		node.transition()
			.duration(500)
			.attr("r", function(d){
				var times = d.changeTimes[dateIdx];
				if (times == 0)
					return 0;
				return rScale(times);
			})
		currentDateIdx = dateIdx;
		stopTransitFlag = false;
		console.log("Jump transition");
		transit();
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
		initializeClusters();
		updateClusters(1);
		initializeNodes();
		rScale.domain([0, datesData.length / 2 + 1]);
		chargeForceScale.domain([0, datesData.length / 2 + 1]);

		setTimeout(function(){
			pos.moveToFront();	
			stopTransitFlag = false;		
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
};
