/*
 * basemetric.h
 *
 *  Created on: Jun 18, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef BASEMETRIC_H_
#define BASEMETRIC_H_

#include <string>
#include "autil.h"
#include "metricproperties.h"

namespace plethora {

class BaseMetric {
public:
	BaseMetric() { } ;
	virtual ~BaseMetric() { };
	virtual std::string getName() = 0;
	virtual std::string getValueString() = 0;
	virtual MetricProperties *getProperties() = 0;
	virtual Enums::METRICTYPE getValueType() = 0;
};

} /* namespace plethora */

#endif /* BASEMETRIC_H_ */
