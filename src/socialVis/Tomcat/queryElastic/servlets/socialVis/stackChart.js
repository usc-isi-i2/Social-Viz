function drawBarChart(index, data){
    var stack = d3.layout.stack(); 
    var len = data.length;                                        //stack layout
    var barChartWidth = size * 5;
    var barChartMargin = 20;
    var barChartHeight = 70;

    var history = new Array();                                             //reformulate the data in the manner of stack layout
    for (var i = 0; i <= data[0].length; i++){
        history.push(new Array());
        if (i == 0){
            for (var j = 0; j < data.length; j++){
                history[i].push({x : j, y : data[j][index]});
            }
            continue;
        }
        for (var j = 0; j < data.length; j++){
            history[i].push({x : j, y : data[j][i - 1]});
        }
    } 

    history.splice(index + 1, 1);

    layers = stack(history);                                              //this will generate y0 in each object

    var maxY = d3.max(layers, function(layer){                            
        return d3.max(layer, function(d){ 
            return d.y0 + d.y; 
        }); 
    });

    var xScale = d3.scale.ordinal()                                       //return the x position of bar
        .domain(d3.range(size))
        .rangeRoundBands([0, barChartWidth], .1);
 
    var yScale = d3.scale.linear()                                        //return y position of bar
        .domain([0, maxY])
        .range([barChartHeight, 0]);

    layer = svg.selectAll(".layer");
    layer = layer.data(layers, function(d, i){
        return i;
    });

    layer.enter()
        .append("g")
        .attr("class", "layer");

    layer.transition()
        .duration(500)
        .delay(function(d,i){
            return i * 10;
        })
        .style("fill", function(d, i){ 
            var tmpIndex = 0;
            if (i == 0){
                tmpIndex = index;
            } else if (i <= index){
                tmpIndex = i - 1;
            } else {
                tmpIndex = i;
            }
            //console.log(tmpIndex);
            return cScale(tmpIndex + 1); 
        });

    rect = layer.selectAll("rect") 
        .data(function(d){ 
            return d; 
        });

    rect.enter()
        .append("rect")
        .attr("x", function(d){ 
            return xScale(d.x); 
        })
        .attr("y", barChartHeight)
        .attr("width", xScale.rangeBand())
        .attr("height", 0);

    rect.transition()
        .duration(500)
        .delay(function(d, i){ 
            return i * 10; 
        })
        .attr("y", function(d){ 
            return yScale(d.y0 + d.y) + window.innerHeight - 60 - barChartHeight; 
        })
        .attr("height", function(d){ 
            return yScale(d.y0) - yScale(d.y0 + d.y); 
        });
}



