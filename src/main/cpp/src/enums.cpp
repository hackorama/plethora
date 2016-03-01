/*
 * enums.cpp
 *
 *  Created on: Jun 28, 2013
 *      Author: Kishan Thomas <kishan.thomas@gmail.com>
 */
#include "enums.h"

namespace plethora {

const std::vector<std::string> Enums::TYPES = Enums::initTypes();
const std::vector<std::string> Enums::LEVELS = Enums::initLevels();
const std::vector<std::string> Enums::ATTRIBUTES = Enums::initAttributes();

}  /* namespace plethora */
