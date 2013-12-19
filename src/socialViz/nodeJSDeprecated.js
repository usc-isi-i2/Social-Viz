var jsdom = require('jsdom'),
scripts = ["file://"+__dirname+"/d3.min.js",
"file://"+__dirname+"/d3.layout.min.js",
"file://"+__dirname+"/colorbrewer.v1.min.js",
"file://"+__dirname+"/jquery.js",
"file://"+__dirname+"/createChart.js"];

var fs = require("fs");
var dir = process.argv[3];
var max_count = process.argv[2];
var output_dir = process.argv[4];

var files = fs.readdirSync(dir);
var str,data;

for(var i in files){
	
	htmlStub = '<!DOCTYPE html><body id="display"/>';
	console.log(dir+"/"+files[i]);	
	str =fs.readFileSync(dir+"/"+files[i], 'utf8');
	data = JSON.parse(str);
	createSVG(htmlStub,scripts,data,max_count,output_dir,files[i].substring(0,files[i].indexOf(".")))

}

function createSVG(htmlStub,scripts,data,max_count,output_dir,file){
	jsdom.env({features:{QuerySelector:true}, html:htmlStub, scripts:scripts, done:function(errors, window) {

		var svgsrc = window.visualize("#display",data,max_count).innerHTML;

		try{
			fs.writeFileSync(output_dir+"/"+file+".svg", svgsrc); 
			console.log('File saved!');
		}
		catch(err){
			console.log(err);		
		}
	console.log(svgsrc);
	}});
}
