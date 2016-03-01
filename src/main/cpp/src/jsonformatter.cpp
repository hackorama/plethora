/*
 * jsonformatter.cpp
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#include "jsonformatter.h"

namespace plethora {

const std::string JsonFormatter::PLETHORA_TAG = "plethora";

JsonFormatter::JsonFormatter() {
}

JsonFormatter::~JsonFormatter() {
}

std::string JsonFormatter::type() {
	return ("JSON");
}

std::string JsonFormatter::format(std::string text) {
	return (format(PLETHORA_TAG, text));
}

std::string JsonFormatter::format(std::string name, std::string value) {
	return (jsonQuote(name) + jsonPropNameValueSeparator() + jsonQuote(value));
}

std::string JsonFormatter::format(std::set<std::string> set) {
	std::string buffer;
	buffer.append(jsonArrayBegin());
	for (std::set<std::string>::iterator it = set.begin(); it != set.end();
	        it++) {
		buffer.append(*it);
		if( it != --set.end()) {
			buffer.append(jsonValueSeparator());
		}
	}
	buffer.append(jsonArrayEnd());
	return (buffer);
}

std::string JsonFormatter::format(std::map<std::string, std::string> map) {
	std::string buffer;
	buffer.append(jsonBegin());
	for (std::map<std::string, std::string>::iterator it = map.begin();
	        it != map.end(); it++) {
		buffer.append(format(it->first, it->second));
		if (it != --map.end()) {
			buffer.append(jsonValueSeparator());
			buffer.append(jsonLineFeed());
		}
	}
	buffer.append(jsonEnd());
	return (buffer);
}

std::string JsonFormatter::jsonBegin() {
	return (jsonEntryBegin() + jsonLineFeed());
}

std::string JsonFormatter::jsonEnd() {
	return (jsonLineFeed() + jsonEntryEnd());
}

std::string JsonFormatter::jsonLineFeed() {
	return ("\n");
}

std::string JsonFormatter::jsonEntryBegin() {
	return ("{");
}

std::string JsonFormatter::jsonEntryEnd() {
	return ("}");
}

std::string JsonFormatter::jsonArrayBegin() {
	return ("[");
}

std::string JsonFormatter::jsonArrayEnd() {
	return ("]");
}

std::string JsonFormatter::jsonPropNameValueSeparator() {
	return (" : ");
}

std::string JsonFormatter::jsonValueSeparator() {
	return (", ");
}

std::string JsonFormatter::jsonQuote(std::string text) {
	return ("\"" + text + "\"");
}

} /* namespace plethora */
