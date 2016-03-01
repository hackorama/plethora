#include "../lib/catch.hpp" /* Get CATCH (C++ Automated Test Cases in Headers) from http://catch-lib.net */

#include "../../src/requesthandler.h"
#include "../../src/jsonformatter.h"
#include "../../src/plethora.h"

#include "test_common.h"

using namespace plethora;
using namespace std;

string getResponse(string request) {
	RequestHandler *handler = new RequestHandler(new JsonFormatter());
	string result = handler->handle(request);
	delete handler;
	return (result);
}

TEST_CASE( "Basic handling", "[handler]" ) {
	initMetrics();
	RequestHandler *handler = new RequestHandler(new JsonFormatter());
	string result = handler->handle("notavalidrequest");
	REQUIRE("\"plethora\" : \"204\"" == result);
	delete handler;
	releaseMetrics();
}

TEST_CASE( "Value handling", "[handler]" ) {
	initMetrics();
	string result = getResponse("get=uno");
	REQUIRE("\"uno\" : \"1\"" == result);
	result = getResponse("get=unknown");
	REQUIRE("\"plethora\" : \"204\"" == result);
	releaseMetrics();
}

TEST_CASE( "List handling", "[handler]" ) {
	initMetrics();
	string result = getResponse("list=names");
	releaseMetrics();
}

TEST_CASE( "Set handling", "[handler]" ) {
	initMetrics();
	string result = getResponse("set=uno&with=42");
	//REQUIRE("\"uno\" : \"42\"" == result); //TODO not implemented yet
	CHECK("\"plethora\" : \"204\"" == result);
	releaseMetrics();
}

TEST_CASE( "Map handling", "[handler]" ) {
	initMetrics();
	string result = getResponse("list=metrics");
	result = getResponse("list=props");
	releaseMetrics();
}

TEST_CASE( "Map props handling", "[handler]" ) {
	initMetrics();
	string result = getResponse("props=uno");
	releaseMetrics();
}


TEST_CASE( "Input validation, encoding check", "[handler]" ) {
	string result = getResponse("get=<script>hacker</script>");
	REQUIRE("\"plethora\" : \"204\"" == result);
	releaseMetrics();
}
