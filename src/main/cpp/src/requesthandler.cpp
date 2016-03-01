/*
 * requesthandler.cpp
 *
 *  Created on: Jun 23, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#include "requesthandler.h"

namespace plethora {

const std::string RequestHandler::ARG_GET_METRIC        = "get";     //get=<name>
const std::string RequestHandler::ARG_DATA_FORMAT       = "as";	     //get=<name>&as=<format>
const std::string RequestHandler::ARG_SET_METRIC        = "set";     //set=<name>
const std::string RequestHandler::ARG_METRIC_VALUE      = "with";    //set=<name>&with=<value>
const std::string RequestHandler::ARG_METRIC_PROPS      = "props";   //props=<name>
const std::string RequestHandler::ARG_LIST              = "list";    //list=<name>
const std::string RequestHandler::ARG_LIST_METRICS      = "metrics"; //list=metrics
const std::string RequestHandler::ARG_LIST_METRIC_NAMES = "names";   //list=names
const std::string RequestHandler::ARG_LIST_METRIC_PROPS = "props";   //list=props

const std::string RequestHandler::MSG_DEFAULT = "204"; //successful, no content
const std::string RequestHandler::MSG_ERROR   = "500"; //internal server error
const std::string RequestHandler::PLETHORA_TAG = "plethora";

RequestHandler::RequestHandler(Formatter *formatter) {
	this->formatter = formatter;
}

RequestHandler::~RequestHandler() {
	if(formatter != NULL) {
		delete formatter;
	}
}

std::string RequestHandler::formatData(std::string name) {
	return (formatter->format(name));
}

std::string RequestHandler::formatData(std::string name, std::string value) {
	return (formatter->format(name, value));
}

std::string RequestHandler::formatData(std::set<std::string> set) {
	return (formatter->format(set));
}

std::string RequestHandler::formatData(std::map<std::string, std::string> map) {
	return (formatter->format(map));
}

bool RequestHandler::validMetric(std::string name) {
	return (Metrics::isValidMetric(name));
}

std::string RequestHandler::metricValue(std::string name) {
	return (Metrics::getStringFormated(name));
}

bool RequestHandler::setMetricValue(std::string name, std::string value) {
	// TODO
	// valid metric name
	// for external access must be a settable metric
	// valid metric value type
	// convert value string to typed value
	//Metrics::setValue(name, value);
	return (false);
}

bool RequestHandler::hasArg(std::string arg) {
	return (queryArgs.find(arg) != queryArgs.end());
}

bool RequestHandler::hasArgWithValue(std::string arg) {
	std::map<std::string, std::string>::iterator it = queryArgs.find(arg);
	return (it != queryArgs.end() && ((std::string)it->second).size() > 0);
}

std::string RequestHandler::getArgValue(std::string name) {
	std::map<std::string, std::string>::iterator it = queryArgs.find(name);
	if (it != queryArgs.end()) {
		return (it->second);
	}
	return (NULL);
}

std::string RequestHandler::validateInput(std::string str) {
	std::stringstream valid;
	for (std::string::iterator it = str.begin(); it < str.end(); it++) {
		switch(*it) {
		case '&':
			valid << "&amp;";
			break;
		case '\'':
			valid << "&apos;";
			break;
		case '"':
			valid << "&quot;";
			break;
		case '<':
			valid << "&lt;";
			break;
		case '>':
			valid << "&gt;";
			break;
		default:
			valid << *it;
			break;
		}
	}
	return (valid.str());
}

void RequestHandler::buildQueryArgMap(std::string request) {
	std::vector<std::string> args = autil::split(request, '&');
	for( std::vector<std::string>::const_iterator it = args.begin(); it != args.end(); ++it) {
		std::pair<std::string, std::string> namevalue = autil::pair(*it, '=');
		queryArgs.insert(namevalue);
	}
}

std::string RequestHandler::printMetrics() {
	return (formatData(Metrics::getMetrics()));
}

std::string RequestHandler::printNames() {
	return (formatData(Metrics::getMetricNames()));
}

std::string RequestHandler::printProps(std::string name) {
	return (formatData(Metrics::getMetricProps(name)->asMap()));
}

std::string RequestHandler::printProps() {
	return (formatData(Metrics::getMetricProps()));
}

std::string RequestHandler::handleGet() {
	std::string name = getArgValue(ARG_GET_METRIC);
	if(validMetric(name)) {
		return (formatData(name, metricValue(name)));
	}
	return (handleDefault());
}

std::string RequestHandler::handleSet() {
	std::string name = getArgValue(ARG_SET_METRIC);
	if (hasArgWithValue(ARG_METRIC_VALUE) && validMetric(name)) {
		std::string value = getArgValue(ARG_METRIC_VALUE);
		if(setMetricValue(name, value)) {
			return (formatData(name, value));
		}
	}
	return (handleDefault());
}

std::string RequestHandler::handleList() {
	std::string value = getArgValue(ARG_LIST);
	if(ARG_LIST_METRICS.compare(value) == 0) {
		return (printMetrics());
	} else if(ARG_LIST_METRIC_NAMES.compare(value) == 0) {
		return (printNames());
	} else if(ARG_LIST_METRIC_PROPS.compare(value) == 0) {
		return (printProps());
	}
	return (handleDefault());
}

std::string RequestHandler::handleProps() {
	std::string name = getArgValue(ARG_METRIC_PROPS);
	if(validMetric(name)) {
		return (printProps(name));
	}
	return (handleDefault());
}

std::string RequestHandler::handleDefault() {
	return (formatData(MSG_DEFAULT));
}

std::string RequestHandler::handleError() {
	return (formatData(MSG_ERROR));
}

std::string RequestHandler::handle(std::string request, std::string &response) {
	response = handle(request);
	return (response);
}

std::string RequestHandler::handle(std::string request) {
	buildQueryArgMap(validateInput(request));
	std::string response;
	if (hasArgWithValue(ARG_GET_METRIC)) {
		response = handleGet();
	} else if (hasArgWithValue(ARG_SET_METRIC)) {
		response = handleSet();
	} else if (hasArgWithValue(ARG_LIST)) {
		response = handleList();
	} else if (hasArgWithValue(ARG_METRIC_PROPS)) {
		response = handleProps();
	} else {
		response = handleDefault();
	}
	return (response);
}

} /* namespace plethora */
