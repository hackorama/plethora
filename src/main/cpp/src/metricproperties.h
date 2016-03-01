/*
 * metricproperties.h
 *
 *  Created on: Jun 18, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef METRICPROPERTIES_H_
#define METRICPROPERTIES_H_

#include <string>
#include <map>
#include "enums.h"

namespace plethora {

class MetricProperties {
	static const std::string PROP_DISPLAYNAME;
	static const std::string PROP_OPTIONS;
	static const std::string PROP_DESCRIPTION;

	std::string displayname;
	std::string description;
	Enums::METRICTYPE type;
	Enums::METRICLEVEL level;
	bool readable;
	bool writable;
	std::string buildOptionString();

public:
	MetricProperties();
	virtual ~MetricProperties();

	MetricProperties *setDisplayname(std::string displayname);
	MetricProperties *setDescription(std::string description);
	MetricProperties *setLevel(Enums::METRICLEVEL level);
	MetricProperties *setType(Enums::METRICTYPE type);
	MetricProperties *setReadable();
	MetricProperties *setReadable(bool value);
	MetricProperties *setWritable();
	MetricProperties *setWritable(bool value);
	std::map<std::string, std::string> asMap();

	std::string getDisplayname() {
		return (displayname);
	}
	std::string getDescription() {
		return (description);
	}
	Enums::METRICLEVEL getLevel() {
		return (level);
	}
	Enums::METRICTYPE getType() {
		return (type);
	}
	bool isReadable() {
		return (readable);
	}
	bool isWritable() {
		return (writable);
	}
};

} /* namespace plethora */

#endif /* METRICPROPERTIES_H_ */
