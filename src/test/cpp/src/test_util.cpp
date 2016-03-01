#include "../lib/catch.hpp" /* Get CATCH (C++ Automated Test Cases in Headers) from http://catch-lib.net */

#include "../../src/autil.h"
#include "../../src/enums.h"

using namespace plethora;
using namespace std;

TEST_CASE( "Arg split", "[util]" ) {
	vector<string> args = autil::split("foo=one&bar&zoo=three", '&');
	REQUIRE("foo=one" == args[0]);
	REQUIRE("bar" == args[1]);
	REQUIRE("zoo=three" == args[2]);
}

TEST_CASE( "Arg pair", "[util]" ) {
	pair<string, string> tuple = autil::pair("foo=bar", '=');
	REQUIRE("foo" == tuple.first);
	REQUIRE("bar" == tuple.second);
}

TEST_CASE ( "Numeric check", "[util]" ) {
	REQUIRE(autil::isNumeric("1") == true);
	REQUIRE(autil::isNumeric("0") == true);
	REQUIRE(autil::isNumeric("01") == true);
	REQUIRE(autil::isNumeric("1234567890") == true);
	REQUIRE(autil::isNumeric("1a") == false);
	REQUIRE(autil::isNumeric("a") == false);
	REQUIRE(autil::isNumeric("") == false);
}

TEST_CASE( "Type conversion checks", "[types]" ) {
	REQUIRE(Enums::TYPEOF(Enums::NUMBER) == "number");
	REQUIRE(Enums::TYPEFROM("number") == Enums::NUMBER);
	REQUIRE(Enums::TYPEFROM("fail") == Enums::UNKNOWN_TYPE);
}
