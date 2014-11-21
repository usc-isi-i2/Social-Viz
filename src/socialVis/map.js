WorldMap = function(svgInstance){
	var svg = svgInstance;

	console.log("load map");

	var cities = null;
	var width = null;
	var height = null;
	var projection = null;
	var g = null;
	var x = null;
	var xAxis = null;
	var y = null;
	var svg1 = null;
	var cities = null;
	var states = null;
	var countryTopology = null;
	var events = null;
	var zoomLevel = null;
	var zoom = null;


	function initialize(){
		cities = new Array();
		width = 1460;
       	height = 500;

        projection = d3.geo.equirectangular()
            .center([0, 5])
            .scale(200)
            .rotate([0, 0]);

        svg.attr("id", "mapsvg");

        path = d3.geo.path()
            .projection(projection);

        g = svg.append("g");

        height = window.innerHeight; //100;
        width = window.innerWidth; //50;

        x = d3.scale.ordinal()
            .rangeBands([0, 20], 0.1);

        y = d3.scale.linear()
        .range([0, 20]);

        counties = false;
        states = false;
        events = [];
        zoomLevel = 1;
        

		

		// load and display the World
        d3.json("map/world-topo-min.json", function(error, topology) {

            countryTopology = topology;
            // load and display the cities
            d3.json("map/cities_nested.json", function(error, data) {
                var initialData = new Array();
                var newData = new Array();
                var i = 0;

                cities = [];
                var forXaxis = [];
                for (var i = 0; i < data.length; i++) {
                    var tempCities = d3.nest()
                        .key(function(d) {
                            return d.city;
                        })
                        .entries(data[i].values);
                    var index = -1;
                    for (var j = 0; j < tempCities.length; j++) {
                        if ((index = indexOfCity(tempCities[j].key, cities)) > -1)
                            cities[index].values.concat(tempCities[j].values);
                        else
                            cities = cities.concat(tempCities[j]);
                        forXaxis = forXaxis.concat(tempCities[j].values);
                    }
                }

                events = data;
                y.range([0, d3.max(cities, function(c) {
                    return c.values[0].value;
                })]);
                y.domain([0, d3.max(cities, function(c) {
                    return c.values[0].value;
                })]);

                x.domain(forXaxis.map(function(d) {
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

                var counter = 0;
                var timerId = setInterval(function() {

                    if (counter == events.length)
                        clearInterval(timerId);
                    else {
                        var eventData = events[counter];
                        var oldData = newData;
                        newData = updateData(eventData.values, newData);

                        var bars = svg1.append("g").selectAll(".bar");

                        var targetData = svg1.selectAll(".bar").data(newData, function(d) {
                            return newData.indexOf(d);
                        });

                        targetData.exit().transition().attr("height", 0).attr("y", function(d) {
                            return projection([d.long, d.lat])[1];
                        }).remove();

                        targetData.enter()
                            .append("rect")
                            .attr("class", "bar")
                            .attr("x", function(d, i) {
                                return projection([d.long, d.lat])[0] + x(d.series);
                            })
                            .attr("width", x.rangeBand())
                            .attr("y", function(d) {
                                return projection([d.long, d.lat])[1];
                            })
                            .attr("height", 0)
                            .attr("fill", function(d) {
                            	if(d.series == "A")  return "#FF00FF"; //pink
                            	else if(d.series == "B") return "#6FFF00"; //green
                            	else if(d.series == "C") return "#FE0001"; //red
                            	else if(d.series == "D") return "#FFFF00"; //yellow
                            	else if(d.series == "E") return "#FF4105"; //orange 
                            })
                            .transition()
                            .attr("y", function(d) {
                                return projection([d.long, d.lat])[1] - y(d.value);
                            })
                            .attr("height", function(d) {
                                return y(d.value);
                            });

                        targetData.transition()
                            .duration(1000)
                            .attr("y", function(d) {
                                return projection([d.long, d.lat])[1] - y(d.value);
                            })
                            .attr("height", function(d) {
                                return y(d.value);
                            });
                        counter++;
                    }
                }, events[counter].key * 2000);
            });

            g.selectAll(".countryPath")
                .data(topojson.object(topology, topology.objects.countries)
                    .geometries)
                .enter()
                .append("path")
                .attr("class","countryPath")
                .attr("d", path)
        });

		// zoom and pan
        zoom = d3.behavior.zoom()
            .scaleExtent([0.2, 8])
            .on("zoom", function() {
                g.attr("transform", "translate(" +
                    d3.event.translate.join(",") + ")scale(" + d3.event.scale + ")");
                g.selectAll("path")
                    .attr("d", path.projection(projection));
          
          		if(zoom.scale() < 1){
          			if(zoomLevel == 1){
          			var aggregateArr = new Array();
          			var countries = new Array();		//country+series values
          			events.forEach(function(event){
          				countries = new Array();
          				var obj = new Object();
          				obj["key"] = event.key;
          				obj["values"] = new Array();
          				event.values.forEach(function(value){

          					var index = countries.indexOf(value.country+value.series);
          					if(index == -1){
          						countries.push(value.country+value.series);
          						var valueObj = new Object();
          						valueObj["country"] = value.country;
          						valueObj["series"] = value.series;
          						valueObj["value"] = parseInt(value.value);

                                topojson.object(countryTopology, countryTopology.objects.countries).geometries
                                .forEach(function(geometry){
                                    if(geometry.properties.name == value.country){
                                        centroid = path.centroid(geometry);
                                    }
                                });

          						obj.values.push(valueObj);
          					}
          					else{
          						for(var i = 0; i < obj.values.length; i++){
          							if(obj.values[i].country == value.country && obj.values[i].series == value.series){
          								obj.values[i].value += parseInt(value.value);
          							}
          						}
          					}
          				});
          				aggregateArr.push(obj);
          			});
          			console.log(aggregateArr);

          			//remove bars
          			//find centroid
          			//plot bars
          			g.selectAll([".bar"])
          			.style("opacity","1")
          			.transition()
          			.duration(1000)
          			.style("opacity","0");

          			var counter = 0;
          			/*var timerId = setInterval(function(){
          				svg1.selectAll(".countryBar")
          				.data(aggregateArr[counter].values)
          				.enter()
          				.append("rect")
          				.attr("x", function(d, i) {
                                return projection([d.long, d.lat])[0] + x(d.series);
                            })
                            .attr("width", x.rangeBand())
                            .attr("y", function(d) {
                                return projection([d.long, d.lat])[1];
                            })
                            .attr("height", 0)
                            .attr("fill", function(d) {
                            	if(d.series == "A")  return "#FF00FF"; //pink
                            	else if(d.series == "B") return "#6FFF00"; //green
                            	else if(d.series == "C") return "#FE0001"; //red
                            	else if(d.series == "D") return "#FFFF00"; //yellow
                            	else if(d.series == "E") return "#FF4105"; //orange 
                            })
                            .transition()
                            .attr("y", function(d) {
                                return projection([d.long, d.lat])[1] - y(d.value);
                            })
                            .attr("height", function(d) {
                                return y(d.value);
                            });
          				counter++;
          			},aggregateArr[counter].key * 2000);*/


          			zoomLevel = 0.5;
          			}
          		}
          		if(zoom.scale() > 1 && zoomLevel == 0.5){
          			g.selectAll([".bar"])
          			.style("opacity","0")
          			.transition()
          			.duration(1000)
          			.style("opacity","1");
          			zoomLevel = 1;
          		}

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
				
                svg1.selectAll(".bar")
                    .attr("height", function(d) {
                        return y(d.value) / d3.event.scale;
                    })
                    .attr("y", function(d) {
                        return projection([d.long, d.lat])[1] - y(d.value) / d3.event.scale;
                    })
                    .attr("width", x.rangeBand()/(d3.event.scale))
                    .attr("x", function(d) {
                                return projection([d.long, d.lat])[0] + x(d.series) / d3.event.scale;
                            });
            });

        svg.call(zoom);
	}

	

		function getCoordinates(name) {
            var coord = [];
            for (var i = 0; i < cities.length; i++) {
                if (cities[i].key == name) {
                    coord = projection(cities[i].values[0].lat, cities[i].values[0].long);
                    break;
                }
            }
            return coord;
        }

        function indexOfCity(key, arr) {
            if (arr.length > 0) {
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].key == key) {
                        return i;
                    }
                }
            }
            return -1;
        }

        function updateData(newValues, oldValues) {
            var temp = new Array();
            if (oldValues.length > 0) {
                for (var i = 0; i < oldValues.length; i++) {
                    for (var j = 0; j < newValues.length; j++) {
                        if (newValues[j].lat == oldValues[i].lat && newValues[j].long == oldValues[i].long && newValues[j].series == oldValues[i].series) {
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


	//================================================================================================================
	//***********************************For public method************************************************************
	//================================================================================================================

	this.generateMap = function(){
		initialize();
	}

	this.getClusterCoordinates = function(name){
		return getCoordinates(name);
	}
}