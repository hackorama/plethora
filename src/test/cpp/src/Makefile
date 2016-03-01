#
# Builds the plethora cpp agent unit tests 
#
# Expects the CATCH unit test framework single heafer file at : lib/catch.hpp 
# And the plethora cpp agent application source at : ../../src/
#
# Get CATCH (C++ Automated Test Cases in Headers) header from http://catch-lib.net
# 
# from app source exclude source with main() : plethora-agent.cpp 
# source with main() will be added by CATCH with macro #define CATCH_CONFIG_MAIN
#
# By default builds with gcov/lcov code coverage 


TESTSRC := $(wildcard test*.cpp)
#APPSRC := $(wildcard ../../src/*.cpp) 
APPSRC := $(shell find ../../src/*.cpp | grep -v plethora-agent.cpp) 

EXE := test
VALGRINDLOG := valgrind.out

all: $(TESTSRC) 
	g++ -g --coverage -o $(EXE) $(TESTSRC) $(APPSRC)

test: $(TESTSRC) 
	g++ -g -o $(EXE) $(TESTSRC) $(APPSRC)

clean:
	rm -f $(EXE) 
	rm -f $(VALGRINDLOG)

memcheck: test
	rm -f $(VALGRINDLOG)
	valgrind --read-var-info=yes --track-origins=yes --leak-check=full --log-file=$(VALGRINDLOG)  ./$(EXE)
	cat $(VALGRINDLOG)
