#include "../lib/catch.hpp" /* Get CATCH (C++ Automated Test Cases in Headers) from http://catch-lib.net */

#include "test_common.h"
#include "../../src/metricproperties.h"

using namespace plethora;
using namespace std;

TEST_CASE( "Metrics add", "[metric]" ) {
	initMetrics();
	REQUIRE("1" == Metrics::getStringFormated("uno"));
	releaseMetrics();
}

TEST_CASE( "Metrics access", "[metric]" ) {
	initMetrics();
	REQUIRE("2" == Metrics::getStringFormated("dos"));
	REQUIRE("true" == Metrics::getStringFormated("tres"));
	REQUIRE("four" == Metrics::getStringFormated("quatro"));
	releaseMetrics();
}

TEST_CASE( "Metric read", "[metric]" ) {
	initMetrics();
	BaseMetric *m = Metrics::getMetric("quatro");
	REQUIRE((NULL != m) == true);
	REQUIRE("quatro" == m->getName());
	REQUIRE("four" == m->getValueString());
	REQUIRE(m->getValueType() == 1);
	releaseMetrics();
}

TEST_CASE( "Metric write", "[metric]" ) {
	initMetrics();
	Metric<long> *m =  (Metric<long> *) Metrics::getMetric("testnumber");
	REQUIRE((NULL != m) == true);
	m->setValue(42);
	REQUIRE(42 == m->getValue());
	releaseMetrics();
}

TEST_CASE( "Default metric properties", "[metric]" ) {
	initMetrics();
	BaseMetric *m = Metrics::getMetric("quatro");
	REQUIRE(m->getProperties()->getLevel() == 0);
	REQUIRE(m->getProperties()->getType() == 1);
	REQUIRE(m->getProperties()->getType() == m->getValueType());
	REQUIRE("" == m->getProperties()->getDescription());
	releaseMetrics();
}
