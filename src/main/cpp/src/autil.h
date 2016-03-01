/*
 * util.h
 *
 *  Created on: Jun 13, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef AUTIL_H_
#define AUTIL_H_

#include <stdlib.h>
#include <iostream>
#include <sstream>
#include <string>
#include <cstring>
#include <sstream>
#include <vector>
#include <map>

namespace plethora {

class autil {
	static const char WHITE_SPACE = ' ';
	static const char NULL_TERMINATE = '\0';

public:

	static bool endsWith(const std::string str, const std::string suffix) {
		if (str.empty() || suffix.empty())
			return (false);
		if (suffix.size() > str.size())
			return (false);
		return (0 == str.compare(str.size() - suffix.size(), str.size(), suffix));
	}

	static bool startsWith(const std::string str, const std::string prefix) {
		if (str.empty() || prefix.empty())
			return (false);
		if (prefix.size() > str.size())
			return (false);
		return (0 == str.compare(0, prefix.size(), prefix));
	}

	static char* trim(char *str) {
		while (*str == WHITE_SPACE)
			str++;
		char *end = str + strlen(str) - 1;
		while (*end == WHITE_SPACE)
			end--;
		*(end + 1) = NULL_TERMINATE;
		return (str);
	}

	static char* toLower(char *str) {
		char *result = str;
		while (*str != NULL_TERMINATE) {
			*str = tolower(*str);
			str++;
		}
		return (result);
	}

	static std::string asString(long value) {
		std::stringstream ss;
		ss << value;
		return (ss.str());
	}

	static std::string asString(bool value) {
		return (value ? "true" : "false");
	}

	static std::string asString(std::string value) {
		return (value);
	}

	static std::string asString(char *value) {
		return (value);
	}

	static char *getCopyString(std::string str) {
		char *copystring = new char[str.size() + 1];
		std::copy(str.begin(), str.end(), copystring);
		copystring[str.size()] = '\0';
		return (copystring);
	}

	static std::vector<std::string> split(std::string const &str, const std::string delimiters) {
		std::vector<std::string> result;
		char *input = getCopyString(str);
		char *token = strtok(input, (const char*) delimiters.c_str());
		while (token != NULL) {
			std::cout << token << std::endl;
			result.push_back(token);
			token = strtok(NULL, delimiters.c_str());
		}
		delete[] input;
		return (result);
	}

	static std::vector<std::string> split(std::string const &str, const char delim) {
		std::vector<std::string> result;
		std::stringstream ss(str);
		std::string token;
		while (std::getline(ss, token, delim)) {
			result.push_back(token);
		}
		return (result);
	}

	static std::pair<std::string, std::string> pair(std::string const &str, const char delim) {
		std::pair<std::string, std::string> result;
		std::size_t mark = str.find(delim);
		if(mark == std::string::npos) {
			result.first = str;
			result.second = "";

		} else {
			result.first = str.substr(0, mark);
			result.second = str.substr(mark+1);
		}
		return (result);
	}

	static bool isNumeric(const std::string& str) {
		std::string::const_iterator it = str.begin();
		while (it != str.end() && std::isdigit(*it)) ++it;
		return (!str.empty() && it == str.end());
	}

	static bool isBoolean(const std::string& str) {
		return (std::string("TRUE").compare(str) == 0 || std::string("true").compare(str) == 0 ||
		        std::string("FALSE").compare(str) == 0 || std::string("false").compare(str) == 0 ||
		        std::string("1").compare(str) == 0 || std::string("0").compare(str) == 0 ||
		        std::string("YES").compare(str) == 0 || std::string("yes").compare(str) == 0 ||
		        std::string("NO").compare(str) == 0 || std::string("no").compare(str) == 0 ); //TODO optimize
	}

	static bool getBool(std::string str) {
		return (std::string("TRUE").compare(str) == 0 || std::string("true").compare(str) == 0 ||
		        std::string("YES").compare(str) == 0 || std::string("yes").compare(str)== 0 ||
		        std::string("1").compare(str) == 0); //TODO optimize
	}

	static long getNumber(std::string value) {
		return (atoi(value.c_str()));
	}
	static void debugPrint(std::map<std::string, std::string> map) {
		debugPrint(map, "map");
	}

	static void debugPrint(std::map<std::string, std::string> map, std::string message) {
		std::cout << message << " [" << std::endl;
		for(std::map<std::string, std::string>::iterator it = map.begin(); it != map.end(); it++) {
			std::cout << "\t" << it->first << ", " << it->second << std::endl;
		}
		std::cout << "[" << std::endl;
	}

};

} /* namespace plethora */

#endif /* AUTIL_H_ */
