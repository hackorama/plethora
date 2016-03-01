/*
 * jsonformatter.h
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef JSONFORMATTER_H_
#define JSONFORMATTER_H_

#include <sstream>
#include "formatter.h"

namespace plethora {

class JsonFormatter: public plethora::Formatter {
	static const std::string PLETHORA_TAG;
public:
	JsonFormatter();
	virtual ~JsonFormatter();

	std::string type();
	std::string format(std::string value);
	std::string format(std::string name, std::string value);
	std::string format(std::set<std::string> set);
	std::string format(std::map<std::string, std::string> map);

	std::string jsonBegin();
	std::string jsonEnd();
	std::string jsonLineFeed();
	std::string jsonEntryBegin();
	std::string jsonEntryEnd();
	std::string jsonArrayBegin();
	std::string jsonArrayEnd();
	std::string jsonPropNameValueSeparator();
	std::string jsonValueSeparator();
	std::string jsonQuote(std::string text);
};

} /* namespace plethora */
#endif /* JSONFORMATTER_H_ */
