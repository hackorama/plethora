/*
 * enums.cpp
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */
#ifndef ENUMS_H_
#define ENUMS_H_

#include <algorithm>
#include <string>
#include <vector>

namespace plethora {

class Enums {
public:

	enum METRICTYPE {
	    NUMBER, TEXT, BOOLEAN, UNSPECIFIED, INVALID, UNKNOWN_TYPE
	};

	static std::vector<std::string> initTypes() {
		std::vector<std::string> v;
		v.push_back("number");
		v.push_back("text");
		v.push_back("boolean");
		v.push_back("unspecified");
		v.push_back("invalid");
		return (v);
	}

	static const std::vector<std::string> TYPES;

	static std::string TYPEOF(METRICTYPE type) {
		return (TYPES[type]);
	}

	static METRICTYPE TYPEFROM(std::string thetype) {
		std::transform(thetype.begin(), thetype.end(), thetype.begin(), ::tolower);
		unsigned int index = std::distance(TYPES.begin(), find(TYPES.begin(), TYPES.end(), thetype));
		if(index < TYPES.size()) {
			return ((METRICTYPE)index);
		}
		return (UNKNOWN_TYPE);
	}

	enum METRICLEVEL {
	    PUBLIC, LIMITED, INTERNAL
	};

	static const std::vector<std::string> LEVELS;
	static const METRICLEVEL DEFAULT_LEVEL = PUBLIC;

	static std::vector<std::string> initLevels() {
		std::vector<std::string> v;
		v.push_back("public");
		v.push_back("limited");
		v.push_back("internal");
		return (v);
	}

	static METRICLEVEL LEVELFROM(std::string thelevel) {
		std::transform(thelevel.begin(), thelevel.end(), thelevel.begin(), ::tolower);
		unsigned int index = std::distance(LEVELS.begin(),
		                                   find(LEVELS.begin(), LEVELS.end(), thelevel));
		if (index < LEVELS.size()) {
			return ((METRICLEVEL) index);
		}
		return (DEFAULT_LEVEL);
	}

	static std::string LEVELOF(METRICLEVEL level) {
		return (LEVELS[level]);
	}

	enum ATTRIBUTE {
	    NAME, TYPE, LEVEL, DESCRIPTION
	};

	static const std::vector<std::string> ATTRIBUTES;

	static std::vector<std::string> initAttributes() {
		std::vector<std::string> v;
		v.push_back("name");
		v.push_back("type");
		v.push_back("level");
		v.push_back("description");
		return (v);
	}

	static std::string ATTRIBUTEOF(ATTRIBUTE attribute) {
		return (ATTRIBUTES[attribute]);
	}

};

} /* namespace plethora */

#endif /* ENUMS_H_ */
