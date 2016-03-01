#include "../lib/catch.hpp" /* Get CATCH (C++ Automated Test Cases in Headers) from http://catch-lib.net */

#include "test_common.h"

#include "../../src/formatter.h"
#include "../../src/jsonformatter.h"

using namespace plethora;
using namespace std;

void reportJsonFormatter(string test, string result) {
	//cout << test << " -> " << result << endl;
}

TEST_CASE( "Formmatng tests", "[json]" ) {
	Formatter *json = new JsonFormatter();
	REQUIRE( "\"plethora\" : \"value\"" == json->format("value"));
	REQUIRE( "\"name\" : \"value\"" == json->format("name", "value"));
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
