/*
 * plethora.h
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#ifndef PLETHORA_H_
#define PLETHORA_H_

#include <pthread.h>
#include "channel.h"
#include "jsonformatter.h"
#include "requesthandler.h"

namespace plethora {

class Plethora {
	static Channel *channel;
	static pthread_mutex_t mutex;

public:
	Plethora();
	virtual ~Plethora();
	Plethora(const char *filename);
	static bool initPlethora(const char *filename);
	static void releasePlethora();
	static long getNumberMetric(const char *metric);
	static bool getBooleanMetric(const char *metric);
	static const char *getMetric(const char *metric);
	static const char *getTextMetric(const char *metric);
	static bool setMetric(const char *name, long value);
	static bool setMetric(const char *name, bool value);
	static bool setMetric(const char *name, const char *value);
	static bool incrMetric(const char *name, long delta);
	static bool incrMetric(const char *name);
	static bool decrMetric(const char *name, long delta);
	static bool decrMetric(const char *name);
	static std::string handleRequest(std::string request);
	static const char *handleRequestAlloc(const char *request);
};

} /* namespace plethora */
#endif /* PLETHORA_H_ */
