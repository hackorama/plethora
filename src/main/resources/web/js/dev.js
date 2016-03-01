$(function() {

	if (!window.console) {//console log only if available
		console = {};
		console.log = function() {
		};
	}

	var LOGGING = true;
	var data = [];
	var names = [];
	var textNames = [];
	var boolNames = [];
	var dataPoints = 100;
	var updateInterval = 5 * 1000;
	var metricUrl = "json/get/";
	var metricsUrl = "json/getall";
	var metricNamesUrl = "json/listmetrics/number";
	var textMetricNamesUrl = "json/listmetrics/text";
	var boolMetricNamesUrl = "json/listmetrics/boolean";
	var clicklock = 0
	var lastbutton = null;
	var initialMetric = null;
	var selectedMetric = null;
	var plot = null;

	function consoleLog(msg) {
		if (LOGGING)
			console.log(msg);
	}

	function buildMetricURL(metric) {
		return metricUrl + metric;
	}

	function updateTitle(name) {
		$("#graphtitle").text(name);
	}

	function updateValue(value) {
		$("#graphvalue").text(value);
	}

	function initData(name) {
		data[name] = Array();
		var backinTime = getTime() - dataPoints * updateInterval;
		for (var i = 0; i < dataPoints; i++) {
			data[name][i] = [backinTime, null];
			backinTime += updateInterval;
		}
	}

	function displayTime() {
		var date = new Date();
		return date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds()
	}

	function getTime() {
		return (new Date()).getTime();
	}

	function printData() {
		for (var i = 0; i < names.length; i++) {
			consoleLog(names[i] + " " + data[names[i]].length);
			for (var j = 0; j < dataPoints; j++) {
				consoleLog(data[names[i]][j]);
			}
		}
	}
	
	function updateButton(name, value){
		thebutton = $(jqid(name));
		thebutton.attr("value", name + " = " + value);
	}

	function updateText(name, value){
		console.log("text update : " + name + " = " + value);
		elem = $(jqid(name));
		elem.text(value);
	}
	
	function refreshData(metrics) {
		//printData();
		
		//all the number metrics on graph
		for (var i = 0; i < names.length; i++) {
			name = names[i]
			value = metrics[name]
			dataformetric = data[name]
			for (var j = 0; j < (dataPoints - 1); j++) {
				//shift left all
				dataformetric[j] = [dataformetric[j+1][0], dataformetric[j+1][1]];
			}
			//and insert at end
			dataformetric[dataPoints - 1] = [getTime(), value];
			updateButton(name, value);
		}
		//all the number metrics on graph
		for (var i = 0; i < textNames.length; i++) {
			name = textNames[i]
			value = metrics[name]
			updateText(name, value)
		}
		consoleLog("selected : " + selectedMetric);
		redrawGraph();
		updateTitle(selectedMetric);
		updateValue(metrics[selectedMetric]);
	}

	function onDataReceived(serverdata) {
		consoleLog(serverdata[metric]);
		refreshData(serverdata[metric]);
	}

	function onMetricsReceived(serverdata) {
		consoleLog(serverdata["metrics"]);
		metrics = serverdata["metrics"];
		for (var i = 0; i < names.length; i++) {
			consoleLog(names[i] + " = " + metrics[names[i]]);
		}
		refreshData(metrics);
	}

	function getServerData(url, handler) {
		consoleLog("Accessing " + url);
		serverdata = $.ajax({
			url : url,
			method : 'GET',
			dataType : 'json',
			success : onMetricsReceived
		});
	}

	function makeButton(name) {
		return $('<input>', {
			'type' : 'button',
			'class' : 'getData',
			'value' : name,
			'id' : name
		});
	}

	function makeItem(name) {
		return "<tr><td><span class=\"textmetricname\">" + name + "</span></td><td><span class=\"textmetricvalue\" id=\"" + name + "\"></span></td></tr>";
	}
	
	function onNamesReceived(serverdata) {
		consoleLog(serverdata["metrics"])
		names = serverdata["metrics"]
		for (var i = 0; i < names.length; i++) {
			initialMetric = names[0];
			consoleLog(names[i])
			$('#metrics').append(makeButton(names[i]))
			initData(names[i]);
		}
		selectedMetric = initialMetric;
		consoleLog(initialMetric);
		plot = $.plot($("#placeholder"), data[initialMetric], options);
		clickHandler();
		buttonDisableByValue(initialMetric);
		getServerData(metricsUrl);
		update();
	}

	function onTextNamesReceived(serverdata) {
		consoleLog(serverdata["metrics"])
		textNames = serverdata["metrics"]
		for (var i = 0; i < textNames.length; i++) {
			consoleLog("text name " + textNames[i])
			$('#textmetrics').append(makeItem(textNames[i]))
		}
	}
	
	function getMetricNames() {
		serverdata = $.ajax({
			url : metricNamesUrl,
			method : 'GET',
			dataType : 'json',
			success : onNamesReceived
		});
	}

	function getTextMetricNames() {
		serverdata = $.ajax({
			url : textMetricNamesUrl,
			method : 'GET',
			dataType : 'json',
			success : onTextNamesReceived
		});
	}
	
	// setup control widget
	$("#updateInterval").val(updateInterval).change(function() {
		var v = $(this).val();
		if (v && !isNaN(+v)) {
			updateInterval = +v;
			if (updateInterval < 1)
				updateInterval = 1;
			if (updateInterval > 2000)
				updateInterval = 2000;
			$(this).val("" + updateInterval);
		}
	});

	// setup plot
	var options = {
		grid : {
			//backgroundColor: { colors: ["#fff", "#eee"] }
			backgroundColor : null
		},
		series : {
			shadowSize : 0 // drawing is faster without shadows
		},
		/*
		 yaxis: {
		 min: 0, max: 100
		 },
		 */
		yaxis : {
			min : 0
		},
		xaxis : {
			mode : "time",
			timeformat : "%H:%M:%S UTC",
			minTickSize : [10, "second"] 
		}
	};


	function update() {
		getServerData(metricsUrl);
		if (clicklock == 0)
			setTimeout(update, updateInterval);
	}

	function redrawGraph() {
		plot.setData([data[selectedMetric]]);
		plot.setupGrid()
		plot.draw();
	}

	function jqesc(anid) {
		return anid.replace(/(:|\.)/g, '\\$1');
	}

	function jqid(anid) {
		return '#' + jqesc(anid);
	}

	function buttonDisableByValue(thevalue) {
		lastbutton = $(jqid(thevalue));
		lastbutton.attr("disabled", true);
	}

	getMetricNames();
	getTextMetricNames();

	function clickHandler() {
		$("input.getData").click(function() {
			consoleLog("clicked ..");
			clicklock = 1
			var button = $(this);
			if (lastbutton != null)
				lastbutton.attr("disabled", false);
			lastbutton = button;
			button.attr("disabled", true);
			selectedMetric = button.attr("id");
			clicklock = 0;
			update();
		});
	}

});
