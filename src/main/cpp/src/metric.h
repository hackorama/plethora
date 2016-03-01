/*
 * metric.h
 *
 *  Created on: Jun 18, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef METRIC_H_
#define METRIC_H_

#include <typeinfo>
#include <iostream>
#include "basemetric.h"
#include "autil.h"
#include "metricproperties.h"

namespace plethora {

template<typename T>
class Metric: public BaseMetric {
	std::string name;
	T value;
	MetricProperties *properties;
	Enums::METRICTYPE type;

	void init(std::string name, T value, MetricProperties *properties) {
		this->name = name;
		this->value = value;
		type = resolveType(value);
		this->properties = properties->setType(type);
	}

	Enums::METRICTYPE resolveType(T object) {
		if(typeid(object) == typeid(long)) {
			return (Enums::NUMBER);
		} else if(typeid(object) == typeid(bool)) {
			return (Enums::BOOLEAN);
		} else if(typeid(object) == typeid(std::string)) {
			return (Enums::TEXT);
		}
		return (Enums::INVALID);
	}

	/* TODO
	Enums::METRICTYPE resolveTypeName(T object){
	    std::string otype = typeid(object).name();
	    std::cout << object << " :  " << otype << std::endl;
		if(otype.compare("long") == 0){
			return (Enums::NUMBER);
		}else if(otype.compare("bool") == 0){
			return (Enums::BOOLEAN);
		}else if(otype.compare("string") == 0){
			return (Enums::TEXT);
		}
		return (Enums::INVALID);
	}
	*/

public:
	Metric(std::string name, T value) {
		init(name, value, new MetricProperties());
	}

	Metric(std::string name, T value, MetricProperties *properties) {
		init(name, value, properties);
	}

	~Metric() {
		if (properties != NULL) {
			delete properties;
			properties = NULL;
		}
	}

	T getValue() {
		return (value);
	}

	std::string getName() {
		return (name);
	}

	std::string getValueString() {
		return (autil::asString(value));
	}

	Enums::METRICTYPE getValueType() {
		return (type);
	}

	bool setValue(T value) {
		/* TODO Strict Type Checking
		 * if(type == resolveType(value))
		 * comes with additional overhead
		 */
		this->value = value;
		return (true);
	}

	MetricProperties *getProperties() {
		return (properties);
	}
};

} /* namespace plethora */

#endif /* METRIC_H_ */
