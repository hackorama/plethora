/*
 * metricproperties.cpp
 *
 *  Created on: Jun 18, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#include "metricproperties.h"

using namespace std;

namespace plethora {

const std::string MetricProperties::PROP_DISPLAYNAME = "displayname";
const std::string MetricProperties::PROP_OPTIONS = "options";
const std::string MetricProperties::PROP_DESCRIPTION = "description";

MetricProperties::MetricProperties() {
	type = Enums::TEXT;
	level = Enums::PUBLIC;
	displayname = "";
	description = "";
	readable = true;
	writable = false;
}

MetricProperties::~MetricProperties() {

}

MetricProperties*
MetricProperties::setDisplayname(string displayname) {
	this->displayname = displayname;
	return (this);
}

MetricProperties*
MetricProperties::setDescription(string description) {
	this->description = description;
	return (this);
}

MetricProperties*
MetricProperties::setLevel(Enums::METRICLEVEL level) {
	this->level = level;
	return (this);
}

MetricProperties*
MetricProperties::setType(Enums::METRICTYPE type) {
	this->type = type;
	return (this);
}

MetricProperties*
MetricProperties::setReadable(bool readbale) {
	this->readable = readable;
	return (this);
}

MetricProperties*
MetricProperties::setReadable() {
	return (setReadable(true));
}

MetricProperties*
MetricProperties::setWritable(bool writable) {
	this->writable = writable;
	return (this);
}

MetricProperties*
MetricProperties::setWritable() {
	return (setWritable(true));
}

map<string, string>
MetricProperties::asMap() {
	map<string, string>  map;
	map.insert(pair<string, string>(PROP_DISPLAYNAME, displayname));
	map.insert(pair<string, string>(PROP_DESCRIPTION, description));
	map.insert(pair<string, string>(PROP_OPTIONS, buildOptionString()));
	return (map);
}

string MetricProperties::buildOptionString() {
	string bits;
	/*
	 1. metric value type
	 0 : text (default), 1 : number, 2 : flag
	 */
	if (Enums::NUMBER == type) {
		bits.append("1");
	} else if (Enums::BOOLEAN == type) {
		bits.append("2");
	} else {
		bits.append("0");
	}
	/*
	 2. external access level
	 0 : read only (default), 1 : read and write
	 note : internal access is read and write always
	 */
	writable ? bits.append("1") : bits.append("0");

	/*
	 3. external visibility
	 0 : public (default), 1 : limited,  2 : internal
	 */
	if (Enums::LIMITED == level) {
		bits.append("1");
	} else if (Enums::INTERNAL == level) {
		bits.append("2");
	} else {
		bits.append("0");
	}
	/* 4,5,6,7,8. future options */
	bits.append("0000");
	return (bits);
}

} /* namespace plethora */
