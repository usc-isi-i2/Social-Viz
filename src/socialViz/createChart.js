/*
author: Vaishnavi Dalvi
D3 function to create chart for json data using SVG
*/
function visualize(selector,data, max_count) {
var el = window.document.querySelector(selector);

var svgWidth = 650;
var svgHeight = 650;	// change to max_count for day - matrix
var buffer=20;

var jsonDataArray = data.Data;
var x_range = data.x_axis_range[1]; 
var y_range = data.y_axis_range[1]; 

var xscale = d3.scale.linear()
			   .domain([0,x_range])
			   .range([buffer, svgWidth-buffer]);

var yscale = d3.scale.linear()
               .domain([0,y_range])
               .range([svgHeight-buffer,buffer]);	

var intensity_scale = d3.scale.linear()
						.domain([0,Math.log(max_count)])
						.range([0,10]);
				
var color_brewer_scale = d3.scale.quantize()
						   .domain([0,Math.log(max_count)])
						   .range(colorbrewer.YlOrRd[5]);
//Specifying axes details
var xaxis = d3.svg.axis()
			  .scale(xscale)
			  .orient("bottom");
				
var yaxis = d3.svg.axis()
			  .scale(yscale)
			  .orient("left");
				  			  
//Creating svg element				
var svgContainer = d3.select(el)
					 .append("svg:svg")
					 .attr("width",svgWidth)
					 .attr("height",svgHeight);
					 
var rect = svgContainer.append("svg:rect")
					   .attr("width","100%")
					   .attr("height","100%")
					   .attr("fill","black");
					 
var circles = svgContainer.selectAll("svg:circle")
				          .data(jsonDataArray)
						  .enter()
						  .append("svg:circle")
						  .attr("cx", function(d){return xscale(d.x_position)})
						  .attr("cy", function(d){return yscale(d.y_position)})
						  .attr("r", function(d){return intensity_scale(Math.log(d.count))})
						  .attr("fill", function(d){return color_brewer_scale(Math.log(d.count));})							  
						  .append("svg:title")
   						  .text(function(d) { return d.count; });
   						  
			
//Creating axes
svgContainer.append("g")
			.attr("class","axes")
			.attr("transform","translate(0,"+(svgHeight-buffer)+")")
			.call(xaxis);
				
svgContainer.append("g")
			.attr("class", "axes")
			.attr("transform","translate("+buffer+",0)")
			.call(yaxis);										 

$(".axes path").attr("fill","none");
$(".axes path").attr("stroke","white");
$(".axes path").attr("shape-rendering","crispEdges");

$(".axes text").attr("font-size","8px");
$(".axes text").attr("fill","white");

return el;
}
