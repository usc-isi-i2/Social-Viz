WorldMap = function(svgInstance){
	var svg = svgInstance;

	console.log("load map");

	var projection = null;
	var width = null;
	var height = null;
	var rotate = null;
	var maxlat = null;
	var path = null;
	var zoom = null;
	var b = null;
	var s = null;
	var scaleExtent = null;
	var tlast = null;
	var slast = null;
	var stateG = null;
	var countyG = null;
	var nationG = null;
	var mapSvg = null;
	var states = null;
	var counties = null;

	function initialize(){
		width = window.innerWidth;
	    height = window.innerHeight;
	    rotate = 60,        // so that [-60, 0] becomes initial center of projection
	    maxlat = 83;        // clip northern and southern poles (infinite in mercator)
	    states = false;
	    counties = false;
	    mapSvg = svg.append("svg")
        stateG = mapSvg.append("g")
        countyG = mapSvg.append("g")
        nationG = mapSvg.append("g")

		projection = d3.geo.mercator()
		    .rotate([rotate,0])
		    .scale(1)           
		    .translate([width/2, height/2]);

				// set up the scale extent and initial scale for the projection
		b = mercatorBounds(projection, maxlat);
		s = width/(b[1][0]-b[0][0]);
		scaleExtent = [s, 20*s];

		projection
		    .scale(scaleExtent[0]);

		zoom = d3.behavior.zoom()
		    .scaleExtent(scaleExtent)
		    .scale(projection.scale())
		    .translate([0,0])               // not linked directly to projection
		    .on("zoom", redraw);
		    
		path = d3.geo.path()
		    .projection(projection);

  		mapSvg.call(zoom);
		
		d3.json("map/world-110m2.json", function(error, world) {
		    nationG.attr("id", "map")
		    	.selectAll('path')
		        .data(topojson.feature(world, world.objects.countries).features)
		      	.enter()
		      	.append('path')
		      	.attr("d", path);
		});

		d3.json("map/states_" + "usa" + ".topo.json", function(error, us) {
            stateG.attr("id", "states")
            	// .attr("opacity", 0)
		        .selectAll(".statespath")
		        .data(topojson.feature(us, us.objects.states).features)
		        .enter()
		        .append("path")
		        .attr("id", function(d) {
		            return d.id;
		        })
		        .attr("class", "statespath")
		        .attr("d", path);
	    });

	    d3.json("map/counties_" + "usa" + ".topo.json", function(error, us) {
         	countyG.attr("id", "counties")
         		// .attr("opacity", 0)
	          	.selectAll(".countiespath")
	          	.data(topojson.feature(us, us.objects.counties).features)
	           	.enter()
	           	.append("path")
	          	.attr("id", function(d) {
	              	return d.id;
	           	})
	          	.attr("class", "countiespath")
	          	.attr("d", path);
	    });

		// track last translation and scale event we processed
		tlast = [0,0];
		slast = null;
	}

	function redraw() {
		console.log("redraw")
	    if (d3.event) { 
	        var scale = d3.event.scale,
	            t = d3.event.translate;                
	        
	        // if scaling changes, ignore translation (otherwise touch zooms are weird)
	        if (scale != slast) {
	            projection.scale(scale);
	        } else {
	            var dx = t[0]-tlast[0],
	                dy = t[1]-tlast[1],
	                yaw = projection.rotate()[0],
	                tp = projection.translate();
	        
	            // use x translation to rotate based on current scale
	            projection.rotate([yaw+360.*dx/width*scaleExtent[0]/scale, 0, 0]);
	            // use y translation to translate projection, clamped by min/max
	            var b = mercatorBounds(projection, maxlat);
	            if (b[0][1] + dy > 0) dy = -b[0][1];
	            else if (b[1][1] + dy < height) dy = height-b[1][1];
	            projection.translate([tp[0],tp[1]+dy]);
	        }
	        // save last values.  resetting zoom.translate() and scale() would
	        // seem equivalent but doesn't seem to work reliably?
	        slast = scale;
	        tlast = t;
	    }

	    //console.log("zoom scale is: " + zoom.scale());
	    var zoomScale = zoom.scale() / 300;

	    if (zoomScale > 2 && zoomScale < 4) {
	    	if (!states){
	            stateG.moveToFront()
	            	.transition()
	            	.duration(500)
	            	.attr("opacity", 1)
	            states = true;
	        }
	        if (counties){
		    	countyG.transition()
		    		.duration(300)
		    		.attr("opacity", 0)
		    	counties = false;
	        }
		} 
        else if (zoomScale < 2) {
        	if (states){
        		stateG.transition()
	            	.duration(300)
	            	.attr("opacity", 0)
	            states = false;
            }
            if (counties){
	        	countyG.transition()
		    		.duration(300)
		    		.attr("opacity", 0)
		    	counties = false;
		    }            
      	} 
       	else if (zoomScale > 4) {
       		if (states){
	            stateG.transition()
	            	.duration(300)
	            	.attr("opacity", 0)
	            states = false;
	        }
            if (!counties){
	           	countyG.moveToFront()
	           		.transition()
		    		.duration(500)
		    		.attr("opacity", 1)
		    	counties = true;
		    }
	    		
       	}
	   
       	mapSvg.selectAll("path")
       		.attr("d", path);
	    //notify force layout to change coordination
	    socialVisInstance.mapScaleChange(zoomScale);
	}


	// find the top left and bottom right of current projection
	function mercatorBounds(projection, maxlat) {
	    var yaw = projection.rotate()[0],
	        xymax = projection([-yaw+180-1e-6,-maxlat]),
	        xymin = projection([-yaw-180+1e-6, maxlat]);
	    
	    return [xymin,xymax];
	}

	function getCoordinates(geoCode) {
        return projection([geoCode.long, geoCode.lat]);
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


	//================================================================================================================
	//***********************************For public method************************************************************
	//================================================================================================================

	this.generateMap = function(){
		initialize();
	}

	this.getClusterCoordinates = function(geo){
		return getCoordinates(geo);
	}
}





