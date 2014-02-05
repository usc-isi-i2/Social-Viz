/*
author: Vaishnavi Dalvi
Node.js script that uses jsdom to write D3 output to svg file.
*/
var jsdom = require('jsdom'),
scripts = ["file://"+__dirname+"/d3.min.js",
"file://"+__dirname+"/d3.layout.min.js",
"file://"+__dirname+"/colorbrewer.v1.min.js",
"file://"+__dirname+"/jquery.js",
"file://"+__dirname+"/createChart.js"],
htmlStub = '<!DOCTYPE html><body id="display"/>';

var fs = require("fs");
var file = process.argv[3];

var str =fs.readFileSync(file, 'utf8');
console.log("read file: " + file);
var data = JSON.parse(str);

jsdom.env({features:{QuerySelector:true}, html:htmlStub, scripts:scripts, done:function(errors, window) {

var max_count = process.argv[2];

var svgsrc = window.visualize("#display",data,max_count).innerHTML;

fs.writeFile(process.argv[4], svgsrc, function(err) {
    if(err) {
        console.log(err);
    } else {
    	console.log(process.argv[4]);
        console.log("The file was saved!");
    }
}); 

console.log(svgsrc);
}});
