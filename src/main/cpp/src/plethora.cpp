/*
 * plethora.cpp
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */

#include "plethora.h"

namespace plethora {

Channel *Plethora::channel = NULL;
pthread_mutex_t Plethora::mutex = PTHREAD_MUTEX_INITIALIZER; //TODO destroyed on exit

Plethora::Plethora() {
	initPlethora(NULL);
}

Plethora::Plethora(const char *filename) {
	initPlethora(filename);
}

Plethora::~Plethora() {
	releasePlethora();
	pthread_mutex_destroy(&mutex);
}

/**
 * Initializes plethora using the given configuration options
 * The global plethora metrics store gets initialized thread safely
 *
 * @param filename containing the configuration
 * @return true on valid processing of configuration file
 */
bool Plethora::initPlethora(const char *filename) {
	if(NULL == channel) {
		pthread_mutex_lock(&mutex);
		if(NULL == channel) {
			channel = new Channel(filename);
		}
		pthread_mutex_unlock(&mutex);
	}
	return (NULL != channel);
}


/**
 * Remove metrics and release all resources
 */
void Plethora::releasePlethora() {
	if(NULL != channel) {
		delete channel;
		channel = NULL;
	}
}

/**
 * Set the value for the named metric
 *
 * @param name
 * @param value
 * @return true on success
 */
bool Plethora::setMetric(const char *name, long value) {
	return (channel == NULL ? false : channel->setMetric(name, value));
}

/**
 * Set the value for the named metric
 *
 * @param name
 * @param value
 * @return true on success
 */
bool Plethora::setMetric(const char *name, const char *value) {
	return (channel == NULL ? false : channel->setMetric(name, value));
}

/**
 * Set the value for the named metric
 *
 * @param name
 * @param value
 * @return true on success
 */
bool Plethora::setMetric(const char *name, bool value) {
	return (channel == NULL ? false : channel->setMetric(name, value));
}


/**
 * Increment the value for the named metric by given delta
 *
 * @param name
 * @param delta
 * @return true on success
 */
bool Plethora::incrMetric(const char *name, long delta) {
	return (channel == NULL ? false : channel->incrMetric(name, delta));
}

/**
 * Increment the value for the named metric by one
 *
 * @param name
 * @return true on success
 */
bool Plethora::incrMetric(const char *name) {
	return (incrMetric(name, 1));
}

/**
 * Decrement the value for the named metric by given delta
 *
 * @param name
 * The metric name
 * @param delta
 * The increment to the metric value
 * @return True on success
 */
bool Plethora::decrMetric(const char *name, long delta) {
	return (channel == NULL ? false : channel->decrMetric(name, delta));
}

/**
 * Decrement the value for the named metric by one
 *
 * @param name
 * @return true on success
 */
bool Plethora::decrMetric(const char *name) {
	return (decrMetric(name, 1));
}

const char *Plethora::getMetric(const char *metric) {
	return (channel == NULL ? NULL : channel->getMetric(metric));
}

/**
 * Get the value for the metric
 *
 * @param metric
 * @return the string metric value, or empty string on failure
 */
const char *Plethora::getTextMetric(const char *metric) {
	return (channel == NULL ? "" : channel->getTextMetric(metric));
}

/**
 * Get the value for the metric
 *
 * @param metric
 * @return the number metric value, or 0 on failure
 */
long Plethora::getNumberMetric(const char *metric) {
	return (channel == NULL ? 0 : channel->getNumberMetric(metric));
}

/**
 * Get the value for the metric
 *
 * @param metric
 * @return the boolean metric value
 */
bool Plethora::getBooleanMetric(const char *metric) {
	return (channel == NULL ? false : channel->getBooleanMetric(metric));
}

/**
 * The handler for HTTP request for metric data
 * The returned char array reference must be released by delete[]
 *
 * @param request the HTTP request string reference
 * @return the response string reference, that must be released by delete[]
 */
const char *Plethora::handleRequestAlloc(const char *request) {
	std::string response = handleRequest(std::string(request));
	char *result = new char[response.size() + 1];
	std::copy(response.begin(), response.end(), result);
	result[response.size()] = '\0';
	return (result);
}

/**
 * The handler for HTTP request for metric data
 *
 * @param request the HTTP request string
 * @return the response string 
 */
std::string Plethora::handleRequest(std::string request) {
	RequestHandler *handler = new RequestHandler(new JsonFormatter());
	std::string response =  handler->handle(request);
	delete handler;
	return (response);
}


} /* namespace plethora */
