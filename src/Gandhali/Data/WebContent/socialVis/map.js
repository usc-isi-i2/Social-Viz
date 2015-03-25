WorldMap = function(svgInstance){
	var svg = null;

	console.log("load map");
    var pos = null;
	var delay=null;
	var cities = null;
	var width = null;
	var height = null;
	var projection = null;
	var g = null;
	var x = null;
	var xAxis = null;
	var y = null;
	var yCountry = null;
	var svg1 = null;
	var cities = null;
	var states = null;
	var countryTopology = null;
	var events = null;
	var zoomLevel = null;
	var zoom = null;
	var geoData = null;
	var countryData = new Array();
	var placeData = null;
	var allData = null;
	var newData = null;
	var tip=null
	var colors = null;
	var timerId=null;
	var counter=null;

	function setTimer(){
		return timerId;
	}
	
	function clearTimer(){
		clearInterval(this.timerId);
		console.log(this.counter);
		return this.counter;
	}

	function initialize(data){
		cities = new Array();
		this.timerId=null;
		//width = 1460;
       	//height = 500;
        height = window.innerHeight; 
        width = window.innerWidth; 
		mainContainer = document.getElementById("mainContainer");
		while (mainContainer.hasChildNodes()) {
		    mainContainer.removeChild(mainContainer.lastChild);
		}
		svg = d3.select("#mainContainer")                          							
	    	.append("svg")
	    	.attr("width",width)
	    	.attr("height",height)
			.attr("preserveAspectRatio", "xMidYMid");     //for map
	    	//.on("mousemove", mousemove);
		
		pos = svg.append("text")
			.attr("fill", "gold")
			.attr("font-size", 10);
		
		
		
        projection = d3.geo.equirectangular()
            .center([-100, 40])
		.scale(600);
           // .rotate([0, 0]);

        svg.attr("id", "mapsvg");

        path = d3.geo.path()
            .projection(projection);

        g = svg.append("g");

       

        x = d3.scale.ordinal()
            .rangeBands([0, 80]);

        y = d3.scale.linear()
        	.range([0, 20]);

        yCountry = d3.scale.linear()
        	.range([0, 100]);

        counties = false;
        states = false;
        events = [];
        zoomLevel = 1;
		this.counter = 0;
        this.delay=document.getElementById("delay").value;
		color=d3.scale.category10();
		//colorR = color.range();
		
	  	tip = d3.tip()
	    .attr('class', 'd3-tip')
	    .offset([-10, 0])
	    .html(function(d) {
	      return "<span style='color:white'>"+d.series+": " + d.value + "</span>";
	    })
		svg.call(tip);
		
        d3.json("map/places1.json", function(error, latlongData) {
        		geoData = latlongData;
        });
		
		

		// load and display the World
        d3.json("map/world-topo-min.json", function(error, topology) {

            countryTopology = topology;
            g.selectAll(".countryPath")
                .data(topojson.object(topology, topology.objects.countries)
                    .geometries)
                .enter()
                .append("path")
                .attr("class","countryPath")
                .attr("d", path)
            // load and display the cities
         //   d3.json("map/age_formatted.json", function(error, data) {
            	events = data.output;
                var initialData = new Array();
                this.newData = new Array();
                var i = 0;

                var nest = nestData("place");
                cities = nest[0];
                allData = nest[1];
                
                y.range([0, d3.max(cities, function(c) {
                    return c.values[0].value*10;
                })]);
                y.domain([0, d3.max(cities, function(c) {
                    return c.values[0].value;
                })]);

                x.domain(allData.map(function(d) {
                    return d.series;
                }));
                //x.rangeBands([projection(d.long, d.lat)[0], projection(d.long, d.lat)[0] + 50],.1);

                svg1 = g.selectAll(".graph")
                    .data(cities)
                    .enter()
                    .append("svg:svg")
                    .attr("width", width)
                    .attr("height", height)
                    .attr("class", "graph")
                    .append("g");
                // .attr("transform", "translate(" + 20 + "," + 45 + ")");

				loadViz(0);
                
            });
			
			//setTimer(timerId);

		// zoom and pan
  	  	
        zoom = d3.behavior.zoom()
            .scaleExtent([0.8, 6])
            .on("zoom", function() {
			/*	var coordinates = [0, 0];
				coordinates = d3.mouse(this);
				var x = coordinates[0];
				var y = coordinates[1];
				var translate = projection.translate();
	    	projection.translate([
	      		translate[0] - x + width / 2,
	      		translate[1] - y + height / 2
	    	]);*/
                g.attr("transform", "translate(" +
                    d3.event.translate.join(",") + ")scale(" + d3.event.scale + ")");
                g.selectAll(".countryPath")
                    .attr("d", path.projection(projection));

                    if(zoom.scale() < 1 && zoomLevel == 1){

                    	//remove current data from UI
                   /* 	g.selectAll([".bar"])
          				.transition()
          				.duration(1000)
          				.style("opacity","0");
          				zoomLevel = 0.5;

          				if(countryData.length == 0){

                    	 //merge data on country level
                    	var stateNames = Object.keys(geoData.states);
                    	events.forEach(function(event){
                    		for(var i=0; i<event.values.length; i++){
                    			stateNames.forEach(function(state){
                    			if(geoData.states[state].places.indexOf(event.values[i].place) > -1)
                    				var data = event.values[i];
                    				data["country"] = state;
                    				event.values.splice(i,1,data);
                    			});
                    		}
                    	});

                    	var nest = nestData("country");
                    	var countries = JSON.parse(JSON.stringify(nest[0]));
                    	var separateObjs = JSON.parse(JSON.stringify(nest[1]));
                    	var aggregateData = [];

                    	var nestedCountryData = d3.nest()
                    					.key(function(d){return d.country})
                    					.key(function(d){return d.series})
                    					.entries(separateObjs);

                    	nestedCountryData.forEach(function(country){
                    		country.values.forEach(function(series){
                    			var obj = new Object();
                    			obj["country"] = country.key;
                    			obj["series"] = series.key;
                    			obj["value"] = 0;
                    			for(var i=0; i<series.values.length; i++){
                    				obj.value += parseInt(series.values[i].value);
                    			}
                    			countryData.push(obj);
                    		})
                    	});

                    	yCountry.range([0, d3.max(countries, function(c) {
                    		return c.values[0].value/10;
                		})]);
                		yCountry.domain([0, d3.max(countries, function(c) {
                    		return c.values[0].value;
                		})]);
                  
          				svg1 = g.selectAll(".countryGraph")
                    	.data(countryData)
                    	.enter()
                    	.append("svg:svg")
                    	.attr("width", width)
                    	.attr("height", height)
                    	//.attr("class", "graph")
                    	.append("g"); 

                    	svg1.selectAll(".countryBar")
                    		.data(countryData)
                    		.enter()
                    		.append("rect")
                            .attr("class", "countryBar")
                            .attr("x", function(d, i) {
                                return projection([geoData.states[d.country].long, geoData.states[d.country].lat])[0] + x(d.series);
                            })
                            .attr("width", x.rangeBand())
                            .attr("y", function(d) {
                                return projection([geoData.states[d.country].long, geoData.states[d.country].lat])[1];
                            })
                            .attr("height", 0)
                            .attr("fill", function(d) {
                            	return getColor(d.series);
                            })
                            .transition()
                            .attr("y", function(d) {
                                return projection([geoData.states[d.country].long, geoData.states[d.country].lat])[1] - yCountry(d.value);
                            })
                            .attr("height", function(d) {
                                return yCountry(d.value);	
                            });
                        }
                        else{
                        	g.selectAll([".countryBar"])
          					.style("opacity","0")
          					.transition()
          					.duration(1000)
          					.style("opacity","1");
                        } */
                    }
					if(zoom.scale() > 1 && zoomLevel == 0.5){
          		/*		g.selectAll([".bar"])
          				.style("opacity","0")
          				.transition()
          				.duration(1000)
          				.style("opacity","1");

          				g.selectAll([".countryBar"])
          				.style("opacity","1")
          				.transition()
          				.duration(1000)
          				.style("opacity","0");
          				zoomLevel = 1; */
          			}


                    //load states and cities

                    if (zoom.scale() > 2 && zoom.scale() < 4) {
                    if (!states) {

                        d3.json("map/states_" + "usa" + ".topo.json", function(error, us) {
                            g.append("g")
                                .attr("id", "states")
                                .selectAll(".statespath")
                                .data(topojson.object(us, us.objects.states).geometries)
                                .enter()
                                .append("path")
                                .attr("id", function(d) {
                                    return d.id;
                                })
                                .attr("class", "active")
                                .attr("d", path);
                            states = true;
                            g.selectAll(".graph, #states").sort(function(a,b){
								if(a != undefined && b==undefined) //a:state,b:graph
									return 1;
								else 
									return -1;
							});
                            //g.selectAll("#" + d.id).style('display', 'none');
                        });
                    }
                    if(counties){
                    	g.selectAll(["#counties"]).remove();
                    	counties = false;
                    }
                } else if (zoom.scale() < 2 && states) {
                    //g.selectAll("#" + country.id).style('display', null);
                    g.selectAll(["#states"]).remove();
                    states = false;
                } else if (zoom.scale() > 4) {
                	if(!counties){
                    	d3.json("map/counties_" + "usa" + ".topo.json", function(error, us) {
                        	g.append("g")
                        		.attr("id", "counties")
                                .selectAll(".countiespath")
                                .data(topojson.object(us, us.objects.counties).geometries)
                                .enter()
                                .append("path")
                                .attr("id", function(d) {
                                    return d.id;
                                })
                                .attr("class", "active")
                                .attr("d", path);
                            counties = true;
                            g.selectAll(".graph, #states, #counties").sort(function(a,b){
								if(a != undefined && b==undefined) //a:state,b:graph
									return 1;
								else 
									return -1;
							});    
                    	});
                	}
                }


                //modify bars on zoom

                g.selectAll([".bar"])
                    .attr("height", function(d) {
                        return y(d.value) / d3.event.scale;
                    })
                    .attr("y", function(d) {
                        return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[1] - y(d.value) / d3.event.scale;
                    })
                    .attr("width", x.rangeBand()/(d3.event.scale))
                    .attr("x", function(d) {
                                return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[0] + x(d.series) / d3.event.scale;
                            });	
            });

       // var coords = getCoordinates({"lat":"47.61","long":"-122.33"});
        svg.call(zoom);
	}


	function loadViz(count){
		var playindex=0;
		this.counter=count;
		if(this.timerId)
			clearInterval(this.timerId);
        this.timerId = setInterval(function() {

            if (this.counter == events.length)
                clearInterval(this.timerId);
            else {
                var eventData = events[this.counter];
				document.getElementById("title").innerHTML="Current Date: "+eventData.key;
				
                var oldData = [];
				if(this.counter>0)
					oldData = events[this.counter-1].values;
                this.newData = updateData(eventData.values, this.newData);

                var bars = svg1.append("g").selectAll(".bar");

				var that = this;
                placeData = svg1.selectAll(".bar").data(that.newData, function(d) {
                    return that.newData.indexOf(d);
                });

                placeData.exit().attr("fill", function(d) {
                    	return d.color;
                    }).transition().duration(that.delay)
				.attr("height", 0)
				.attr("y", function(d) {
                    return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[1];
                }).remove();

                placeData.enter()
                    .append("rect")
                    .attr("class", "bar")
                    .attr("x", function(d, i) {
                        return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[0] + x(d.series);
                    })
                    .attr("width", x.rangeBand())
                    .attr("y", function(d) {
                        return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[1];
                    })
                    .attr("height", 0)
					.on('mouseover', tip.show)
				  	.on('mouseout', tip.hide)
                    .attr("fill", function(d) {
                    	return color(d.series);//d.color;
                    })
                    .transition()
					 .duration(that.delay)
                    .attr("y", function(d) {
                        return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[1] - y(d.value);
                    })
                    .attr("height", function(d) {
                        return y(d.value);
                    });

                placeData.transition()
                    .attr("fill", function(d) {
                    	return d.color;
                    })
                    .duration(that.delay)
                    .attr("y", function(d) {
                        return projection([geoData.places[d.place].long, geoData.places[d.place].lat])[1] - y(d.value);
                    })
                    .attr("height", function(d) {
                        return y(d.value);
                    });
					
					placeData.filter(function(d){
						if(oldData.indexOf(d)==-1)
							return false;
						return oldData[oldData.indexOf(d)].value === d.value;
					})
					.style("fill",function(d){
						var hsl = d3.hsl(d.color);
						hsl.l=0.2;
						return hsl.toString();
					})
					.on('mouseover', tip.show)
				  	.on('mouseout', tip.hide);
                this.counter++;
				playindex++;
            }
        }, (playindex+1) * this.delay*2);
	}

	
		function getColor(series){
			if(series == "A")  return "#FF00FF"; //pink
            else if(series == "B") return "#6FFF00"; //green	

            else if(series == "C") return "#FE0001"; //red
            else if(series == "D") return "#FFFF00"; //yellow
            else if(series == "E") return "#FF4105"; //orange 
		}

		function getCoordinates(geoCode) {
            var coord = projection([geoCode.long, geoCode.lat]);
            return coord;
        }

        function indexOfObject(key, arr) {
            if (arr.length > 0) {
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].key == key) {
                        return i;
                    }
                }
            }
            return -1;
        }


        function nestData(param){
        		nestedData = [];
                var forXaxis = [];
                for (var i = 0; i < events.length; i++) {
                    var paramData = d3.nest()
                        .key(function(d) {
                            return d[param];
                        })
                        .entries(events[i].values);
                    var index = -1;
                    for (var j = 0; j < paramData.length; j++) {
                        if ((index = indexOfObject(paramData[j].key, nestedData)) > -1)
                            nestedData[index].values.concat(paramData[j].values);
                        else
                            nestedData = nestedData.concat(paramData[j]);
                        forXaxis = forXaxis.concat(paramData[j].values);
                    }
                }

                return [nestedData, forXaxis];
        }

        function updateData(newValues, oldValues) {
            var temp = new Array();
            if (oldValues.length > 0) {
                for (var i = 0; i < oldValues.length; i++) {
                    for (var j = 0; j < newValues.length; j++) {
                        if (geoData.places[[newValues[j].place]].lat == geoData.places[[oldValues[i].place]].lat && geoData.places[[newValues[j].place]].long == geoData.places[[oldValues[i].place]].long && newValues[j].series == oldValues[i].series) {
                            oldValues.splice(i, 1, newValues[j]);
                        } else
                        if (temp.indexOf(newValues[j]) < 0)
                            temp.push(newValues[j]);
                    }
                }
                oldValues = oldValues.concat(temp);
            } else oldValues = newValues;
            return oldValues;
        }
		
		function mousemove(){
			var ary = d3.mouse(this);
			pos.attr("x", ary[0] + 2)
				.attr("y", ary[1] + 2)
				//.attr("x", 100)
				//.attr("y", 100)
				.text(Math.round(ary[0]) + ", " + Math.round(ary[1]))
		}


	//================================================================================================================
	//***********************************For public method************************************************************
	//================================================================================================================

	/*this.timer = function(){
		return setTimer();
	}*/
	
	this.pause = function(){
		return clearTimer();
	}
	
	this.start = function(count){
		loadViz(count);
	}

	this.generateMap = function(data){
		initialize(data);
	}

	this.getClusterCoordinates = function(name){
		return getCoordinates(name);
	}
}