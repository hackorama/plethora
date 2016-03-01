//============================================================================
// Name        : plethora-agent.cpp
// Author      : Kishan Thomas <kishan.thomas@gmail.com> Kishan Thomas <kishan.thomas@gmail.com>
// Version     :
// Copyright   : © Copyright 2013 Hewlett-Packard Development Company, L.P.
// Description : Plethora Agent Test
//============================================================================

#include <iostream>
#include "plethora.h"
#include "jsonformatter.h"

using namespace std;

#define GCC_VERSION (__GNUC__ * 10000 + __GNUC_MINOR__ * 100 + __GNUC_PATCHLEVEL__)

void testPlatform() {

	cout << "cpp: " << __cplusplus << endl;
	cout << "gcc: " << __VERSION__ << endl;
	cout << "gcc: " << GCC_VERSION << endl;

}

void testUtilSplit() {
	vector<string> args = plethora::autil::split("foo=one&bar&zoo=three", '&');
	for (vector<string>::const_iterator it = args.begin(); it != args.end();
	        ++it) {
		pair<string, string> tuple = plethora::autil::pair(*it, '=');
		cout << *it << " : " << tuple.first << ", " << tuple.second << endl;
	}
}

void testMetrics() {
	using plethora::Metric;
	using plethora::BaseMetric;
	using plethora::Metrics;

	Metrics::addMetric((BaseMetric*) new Metric<long>("uno", 1));
	Metrics::addMetric((BaseMetric*) new Metric<long>("dos", 2));
	Metrics::addMetric((BaseMetric*) new Metric<bool>("tres", true));
	Metrics::addMetric((BaseMetric*) new Metric<string>("quatro", "four"));
	cout << "uno = " << Metrics::getStringFormated("uno") << endl;
	cout << "dos = " << Metrics::getStringFormated("dos") << endl;
	cout << "tres = " << Metrics::getStringFormated("tres") << endl;
	cout << "quatro = " << Metrics::getStringFormated("quatro") << endl;
	BaseMetric *m = Metrics::getMetric("quatro");
	cout << "getName : " << m->getName() << endl;
	cout << "getValueType : " << m->getValueType() << endl;
	cout << "getValueString : " << m->getValueString() << endl;
	cout << "getType : " << m->getProperties()->getType() << endl;
	cout << "getLevel : " << m->getProperties()->getLevel() << endl;
	cout << "getDescription : " << m->getProperties()->getDescription() << endl;
}

void testHttpArgParser() {
	bool res = plethora::Plethora::initPlethora(NULL);
	cout << "RES1 = " << res << endl;
	//cout << "handled " << plethora::Plethora::handleRequest("foo=one&bar&zoo=three") << endl;
}

void testPlethoraConfigFile(char *filename) {
	bool res = plethora::Plethora::initPlethora(filename);
	cout << "RES2 = " << res << endl;
}

void testResponse(char *request) {
	const char  *res = plethora::Plethora::handleRequestAlloc(request);
	cout << "response:" << res << endl;
	delete []res;
	//cout << "response:" << plethora::Plethora::handleRequest(request) << endl;
}

void testResponse2(std::string request) {
	std::string res = plethora::Plethora::handleRequest(std::string(request));
	cout << "response:" << res << endl;
}
void testHandler() {

	testResponse((char *)"list=props");
	testResponse((char *)"list=props");
	testResponse2("list=props");
	cout << "response:" << plethora::Plethora::handleRequest("list=metrics") <<endl;
	cout << "response:" << plethora::Plethora::handleRequest("list=names") <<endl;
	cout << "response:" << plethora::Plethora::handleRequest("get=uno") <<endl;
	cout << "response:" << plethora::Plethora::handleRequest("props=uno") <<endl;

	cout << "response:" << plethora::Plethora::handleRequest("list=unknown") <<endl;
	cout << "response:" << plethora::Plethora::handleRequest("get=unknown") <<endl;
	cout << "response:" << plethora::Plethora::handleRequest("props=unknown") <<endl;
}
void reportJsonFormatter(string test, string result) {
	cout << test << " -> " << result << endl;
}
void testJsonFormatter() {
	plethora::Formatter *json = new plethora::JsonFormatter();
	reportJsonFormatter("single value", json->format("value"));
	reportJsonFormatter("a name, value pair", json->format("name", "value"));
	set<string> list;
	list.insert("uno");
	list.insert("dos");
	list.insert("tres");
	reportJsonFormatter("list of values", json->format(list));
	map<string, string> pairmap;
	pairmap.insert(std::pair<string, string>("1", "uno"));
	pairmap.insert(std::pair<string, string>("2", "dos"));
	pairmap.insert(std::pair<string, string>("3", "tres"));
	reportJsonFormatter("list of name,value pairs", json->format(pairmap));
	set<string> emptylist;
	reportJsonFormatter("list of name,value pairs", json->format(emptylist));
	map<string, string> emptymap;
	reportJsonFormatter("list of name,value pairs", json->format(emptymap));
	delete json;
}

void testInstance() {
	plethora::Plethora *plethora = new plethora::Plethora();
	const char *res = plethora->getTextMetric("configtext");
	cout << res << endl;
	delete plethora;
}

int main() {
	testPlatform();
	//testPlethoraConfigFile((char*) "../tests/src/test.config.prop");
	testPlethoraConfigFile((char*) "tests/src/test.config.prop");
	testMetrics();
	testHandler();
	testHttpArgParser();
	testJsonFormatter();
	plethora::Plethora::releasePlethora();
	testInstance();
	return (0);
}
