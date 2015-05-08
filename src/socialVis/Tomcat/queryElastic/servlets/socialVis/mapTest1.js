WorldMap = function(svgInstance){
	var svg = svgInstance;

	console.log("load map");

	var projection = null;
	var width = null;
	var height = null;
	var rotate = null;
	var path = null;
	var zoom = null;
	var defaultTrans = null;
    var defaultScale = null;
    var countryG = null;
    var statesG = null;
    var statesLabelG = null;
    // var countryLabelG = null;

	function initialize(){
		width = parseInt($("#rightPanel").css("width"));
	    height = window.innerHeight;
	    rotate = [100,0],        // so that [-60, 0] becomes initial center of projection
	
		projection = d3.geo.mercator()
		    .rotate(rotate)
		    .scale(600)           
		    .translate([width/2, height]);
		defaultTrans = projection.translate();
    	defaultScale = projection.scale();
	

		zoom = d3.behavior.zoom()      
		    .on("zoom", redraw);
		    
		path = d3.geo.path()
		    .projection(projection);

		svg.call(zoom);
		countryG = svg.append("g")
		statesG = svg.append("g") 
		// countryLabelG = svg.append("g")
		statesLabelG = svg.append("g")

		statesLabelG.moveToBack();
		statesG.moveToBack();
		countryG.moveToBack();

		d3.json("map/world-110m2.json", function ready(error, world) {

		    countryG.selectAll('.country')
		        .data(topojson.feature(world, world.objects.countries).features)
		      	.enter()
		      	.append('path')
		      	.attr("class", "country")
		      	.attr('d', path);	    
		   

		  //   countryLabelG
    //      		.attr("id", "countryLabel")
    //          	.selectAll(".country-label")
    //           	.data(topojson.feature(world, world.objects.countries).features)
	   //        	.enter()	                   	
    //            	.append("text")
    //            	.attr("opacity", 1)
    //            	.attr("class", "country-label")
			 //    .attr("dy", ".35em")
			 //    .attr("font-size", "20px")
			 //    .text(function(d) { return d.properties.name; })
			 //    .attr("transform", function(d) { 
				// 	var tmpCentroid = path.centroid(d);
				// 	return "translate(" + tmpCentroid[0] + "," + tmpCentroid[1] + ")"; 
				// });
		});

		d3.json("map/states_" + "usa" + ".topo.json", function(error, us) {
            statesG
	           	.attr("id", "states")
	           	.selectAll(".state")
	           	.data(topojson.feature(us, us.objects.states).features)
	           	.enter()
	          	.append("path")
	          	.attr("class", "state")
	           	.attr("d", path);

	      	statesLabelG
         		.attr("id", "statesLabel")
             	.selectAll(".states-label")
              	.data(topojson.feature(us, us.objects.states).features)
	          	.enter()	                   	
               	.append("text")
               	.attr("opacity", 0)
               	.attr("class", "states-label")
			    .attr("dy", ".35em")
			    .attr("font-size", "12px")
			    .text(function(d) { return d.properties.name; })
			    .attr("x", function(d){
			    	d.width = this.getBBox().width;
			    	return -(d.width / 2);
			    });
	    });
	}

	function redraw() {
	    if (d3.event) { 
	        var tx = defaultTrans[0] * d3.event.scale + d3.event.translate[0];
	      	var ty = defaultTrans[1] * d3.event.scale + d3.event.translate[1];
	      	var scl = defaultScale * d3.event.scale;
	      	projection.translate([tx, ty]);
	      	projection.scale(scl);
	      	statesG.selectAll(".state")
	      		.attr("d", path);
	      	countryG.selectAll(".country")
	      		.attr("d", path);
	    	// console.log("event scale is: " + d3.event.scale);
	    
		    if (d3.event.scale > 2) {
	            statesLabelG.selectAll(".states-label")	
	            	.attr("opacity", 1)
					.attr("transform", function(d) { 
						var tmpCentroid = path.centroid(d);
					  	return "translate(" + tmpCentroid[0] + "," + tmpCentroid[1] + ")"; 
				    })	   
			} else {
				 d3.selectAll(".states-label")	
	            	.attr("opacity", 0)
			}
	     
		    
		    svg.selectAll('path')       // re-project path data
		        .attr('d', path);

		    //notify force layout to change coordination
		    socialVisInstance.mapScaleChange(d3.event.scale);
	    }
	}

	function getCoordinates(geoCode) {
        return projection([geoCode.lon, geoCode.lat]);
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





