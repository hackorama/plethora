#define CATCH_CONFIG_MAIN
#include "../lib/catch.hpp" /* Get CATCH (C++ Automated Test Cases in Headers) from http://catch-lib.net */

#include "../../src/plethora.h"

using namespace plethora;
using namespace std;

TEST_CASE("Plethora config  file", "[plethora]" ) {
	CHECK(Plethora::initPlethora((char *)"test.config.prop") == true);
	Plethora::releasePlethora();
}

TEST_CASE("Plethora instance", "[plethora]" ) {
	Plethora *plethora = new Plethora();
	CHECK("" == string(plethora->getTextMetric("configtext")));
	CHECK("" == string(plethora->getTextMetric("unknown")));
        delete plethora;
}

TEST_CASE("Plethora instance config file", "[plethora]" ) {
	Plethora *plethora = new Plethora((char *)"test.config.prop");
	CHECK("" == string(plethora->getTextMetric("unknown")));
	CHECK("text" == string(plethora->getTextMetric("configtext")));
	CHECK("" == string(Plethora::getTextMetric("unknown")));
	CHECK("text" == string(Plethora::getTextMetric("configtext")));
	string foo = string(plethora->getTextMetric("unknown"));
        delete plethora;
}

TEST_CASE("Plethora number metric", "[plethora]" ) {
	CHECK(Plethora::initPlethora((char *)"test.config.prop") == true);
	CHECK(1 == Plethora::getNumberMetric("confignumber"));
	CHECK(Plethora::setMetric((const char*)"confignumber", (long)40) == true);
	CHECK(40 == Plethora::getNumberMetric("confignumber"));
	CHECK(Plethora::incrMetric("confignumber", 5) == true);
	CHECK(Plethora::decrMetric("confignumber", 5) == true);
	CHECK(Plethora::incrMetric("confignumber", 1) == true);
	CHECK(Plethora::incrMetric("confignumber", 1) == true);
	CHECK(Plethora::incrMetric("confignumber") == true);
	CHECK(Plethora::incrMetric("confignumber") == true);
	CHECK(Plethora::decrMetric("confignumber") == true);
	CHECK(Plethora::decrMetric("confignumber", 1) == true);
	CHECK(42 == Plethora::getNumberMetric("confignumber"));
	Plethora::releasePlethora();
}

TEST_CASE("Plethora text metric", "[plethora]" ) {
	CHECK(Plethora::initPlethora((char *)"test.config.prop") == true);
	CHECK( "text" == string(Plethora::getTextMetric("configtext")));
	CHECK(Plethora::setMetric((const char*)"configtext", "foo") == true);
	CHECK("foo" == string(Plethora::getTextMetric("configtext")));
	Plethora::releasePlethora();
}

TEST_CASE("Plethora bool metric", "[plethora]" ) {
	CHECK(Plethora::initPlethora((char *)"test.config.prop") == true);
	CHECK(true  == Plethora::getBooleanMetric("configbool"));
	CHECK(Plethora::setMetric((const char*)"configbool", false) == true);
	CHECK(false == Plethora::getBooleanMetric("configbool"));
	Plethora::releasePlethora();
}

TEST_CASE( "Handler", "[plethora]" ) {
	CHECK(Plethora::initPlethora((char *)"test.config.prop") == true);
	CHECK(Plethora::setMetric((const char*)"configtext", "superfoo") == true);
	string result = Plethora::handleRequest("get=configtext");
	CHECK("\"configtext\" : \"superfoo\"" == result);
	result = Plethora::handleRequest("get=unknown");
	REQUIRE("\"plethora\" : \"204\"" == result);
	Plethora::releasePlethora();
}

TEST_CASE( "Handler c style", "[plethora]" ) {
	REQUIRE(Plethora::initPlethora((char *)"test.config.prop") == true);
	const char *response = Plethora::handleRequestAlloc("get=configtext");
	CHECK("\"configtext\" : \"text\"" == string(response));
	delete[] response;
	Plethora::releasePlethora();
}
