#include "test_common.h"

using namespace plethora;
using namespace std;

void initMetrics() {
	Metrics::addMetric((BaseMetric*) new Metric<long>("uno", 1));
	Metrics::addMetric((BaseMetric*) new Metric<long>("dos", 2));
	Metrics::addMetric((BaseMetric*) new Metric<bool>("tres", true));
	Metrics::addMetric((BaseMetric*) new Metric<string>("quatro", "four"));
	Metrics::addMetric((BaseMetric*) new Metric<long>("testnumber", 1));
}
void releaseMetrics() {
	Metrics::release();
}
