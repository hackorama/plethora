/*
 * formatter.h
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef FORMATTER_H_
#define FORMATTER_H_

#include <string>
#include <set>
#include <map>
#include "enums.h"

namespace plethora {

class Formatter {
public:
	virtual ~Formatter() { };
	virtual std::string type() = 0;
	virtual std::string format(std::string value) = 0;
	virtual std::string format(std::string name, std::string value) = 0;
	virtual std::string format(std::set<std::string> set) = 0;
	virtual std::string format(std::map<std::string, std::string> map) = 0;
};

} /* namespace plethora */
#endif /* FORMATTER_H_ */
