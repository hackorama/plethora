$(function() {

	if (!window.console) {//console log only if available
		console = {};
		console.log = function() {
		};
		console.info = function() {
		};
	}

	var data = [], totalPoints = 100;
	var updateInterval = 5 * 1000;
	var metric = "";
	var baseurl = "json/get/";
	var namesurl = "json/listmetrics";
	var dataurl = buildURL();
	var clicklock = 0
	var lastbutton = null;
	var initialMetric = "hackorama.tres.files_send";

	function buildURL() {
		dataurl = baseurl + metric;
		return dataurl;
	}

	function updateTitle() {
		$("#graphtitle").text(metric);
	}

	function updateValue(value) {
		$("#graphvalue").text(value);
	}

	function initData(name) {
		metric = name;
		buildURL();
		updateTitle();
		var backinTime = getTime() - totalPoints * updateInterval;
		for (var i = 0; i < totalPoints; i++) {
			data[i] = [backinTime, null];
			backinTime += updateInterval;
		}
		return data;
	}

	function displayTime() {
		var date = new Date();
		return date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds()
	}

	function getTime() {
		return (new Date()).getTime();
	}

	function printData() {
		for (var i = 0; i < totalPoints; i++)
			console.info(data[i]);
	}

	function refreshData(value) {
		for (var i = 0; i < totalPoints - 1; i++)
			data[i] = [data[i+1][0], data[i+1][1]]
		//shift left all
		data[data.length - 1] = [getTime(), value];
		//and insert at end
		printData();
		redrawGraph();
		updateValue(value);
	}

	function getData(dataurl) {
		return getServerData(dataurl);
	}

	function onDataReceived(serverdata) {
		console.info(serverdata[metric]);
		refreshData(serverdata[metric]);
	}

	function getServerData() {
		console.info(dataurl);
		serverdata = $.ajax({
			url : dataurl,
			method : 'GET',
			dataType : 'json',
			success : onDataReceived
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

	function onNamesReceived(data) {
		console.info(data["metrics"])
		names = data["metrics"]
		for (var i = 0; i < names.length; i++) {
			initialMetric = names[0];
			console.info(names[i])
			//$( '#metrics').append('<div>' + names[i] + '</div>')
			$('#metrics').append(makeButton(names[i]))
		}
		clickHandler();
		initData(initialMetric);
		buttonDisableByValue(initialMetric);
		update();
	}

	function getMetricNames() {
		console.info(dataurl);
		serverdata = $.ajax({
			url : namesurl,
			method : 'GET',
			dataType : 'json',
			success : onNamesReceived
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
			backgroundColor: null 
		},
		series : {
			shadowSize : 0
		}, // drawing is faster without shadows
		//yaxis: { min: 0, max: 100 },
		yaxis : {
			min : 0
		},
		xaxis : {
			mode : "time",
			timeformat : "%H:%M:%S",
			ticks : 10 
			//minTickSize : [1,"second"]
		}
	};

	var plot = $.plot($("#placeholder"), [initData()], options);

	function update() {
		getServerData();
		if (clicklock == 0)
			setTimeout(update, updateInterval);
		//loop till url change
	}

	function redrawGraph() {
		plot.setData([data]);
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

	getMetricNames()

	function clickHandler() {
		$("input.getData").click(function() {
			console.info("clicked ..");
			clicklock = 1
			var button = $(this);
			if (lastbutton != null)
				lastbutton.attr("disabled", false);
			lastbutton = button;
			button.attr("disabled", true);
			//name = button.siblings('a').attr('href');
			name = button.val();
			initData(name);
			clicklock = 0;
			update();
		});
	}

});
