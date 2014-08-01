var chartWidth = 20;
var chartHeight = window.innerHeight - 150;
var len = 20;

//var dateScale = d3.scale.linear

var chartXScale = d3.scale.linear()
    .domain([0, 4])
    .range([10, chartWidth * 4 + 10]);

var chartYScale = d3.scale.linear()
    .domain([0, len])
    .range([0, chartHeight]);

var axisYScale = d3.scale.linear()
    .domain([0, 0])
    .range([0, 0]);

var areaChart = d3.svg.area()
    .x(function(d, i) { return chartXScale(d.fraction); })
    .y1(function(d) { return chartYScale(d.index); })
    //.interpolate("linear"); 

var lineChart = d3.svg.line()
    .x(function(d) { return chartXScale(d.fraction); })
    .y(function(d) { return chartYScale(d.index); })
    //.interpolate("linear");  

var isOverChart = -1;

function drawAreaChart(data){
    var lines = chart.selectAll(".lineChart")
        .data(data);
    lines.enter()
        .append("path")
        .attr("class", "lineChart")
        .attr("stroke", function(d, i){
            return cScale(i + 1);
        })
        .attr("id", function(d, i){
        	return "lineChart" + i;
        })
        .attr("stroke-width", 0);
    lines.attr("d", lineChart); 

    areaChart.y0(chartHeight / len * (data[0].length - 1))
    area = chart.selectAll(".areaChart")
        .data(data);
    area.enter()
        .append("path")
        .attr("class", "areaChart")
        .attr("fill", function(d, i){
        	return cScale(i + 1);
        })
        .attr("opacity", 0.5)
        .on("mouseover", function(d, i){
        	isOverChart = i;
        	var obj = d3.select("#lineChart" + i)
        		.attr("stroke-width", 1);
        	var tmpData = obj.datum();
        	for (var j = 0; j < tmpData.length; j++){
        		appendVetialComponent(tmpData[j], i, j);
        	}          	  	
        })
        .on("mouseout", function(d, i){
        	isOverChart = -1;
        	chart.select("#lineChart" + i)
        		.attr("stroke-width", 0);
        	chart.selectAll(".lineChartCircle")
        		.transition()
        		.duration(500)
        		.attr("r", 0)
        		.remove();
        	chart.selectAll(".lineChartText")
        	  	.transition()
        		.duration(500)
        		.attr("opacity", 0)
        		.remove();
        });
    area.attr("d", areaChart);

    if (isOverChart > -1){
    	var lastData = data[isOverChart][data[isOverChart].length - 1];
    	appendVetialComponent(lastData, isOverChart, 0);
    }
}


function appendVetialComponent(tmp, i, j){
	var x = Math.round(chartXScale(tmp.fraction));
    var y = Math.round(chartYScale(tmp.index));
    chart.append("circle")
        .attr("cx", x)
        .attr("cy", y)
        .attr("fill", cScale(i + 1))
        .attr("r", 0)
        .attr("class", "lineChartCircle")
        .transition()
        .duration(500)
        .delay(j * 10)
        .attr("r", 2);

    chart.append("text")
        .attr("x", x + 2)
        .attr("y", y)
        .attr("fill", "white")
        .style("font-size", 10)
        .text((tmp.fraction - i).toPrecision(2))
        .attr("class", "lineChartText")
        .attr("opacity", 0)
        .transition()
        .duration(500)
        .delay(j * 10)
        .attr("opacity", 1);
}

function appendHorizontalComponent(tmp, i){
	var x = Math.round(chartXScale(tmp.fraction));
    var y = Math.round(chartYScale(tmp.index));
    chart.append("circle")
        .attr("cx", x)
        .attr("cy", y)
        .attr("fill", cScale(i + 1))
        .attr("r", 0)
        .attr("class", "lineChartCircleH")
        .transition()
        .duration(500)
        .attr("r", 2);

    chart.append("text")
        .attr("x", x - 5)
        .attr("y", y - 5)
        .attr("fill", "white")
        .style("font-size", 10)
        .text(((tmp.fraction - i).toPrecision(2) + "").substr(1))
        .attr("class", "lineChartTextH")
        .attr("opacity", 0)
        .transition()
        .duration(500)
        .attr("opacity", 1);
}




    