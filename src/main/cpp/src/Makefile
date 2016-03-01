#
# Builds the plethora cpp agent and a test binary
#

SRC := $(wildcard *.cpp)
EXE := agent 
VALGRINDLOG := valgrind.out

all: $(SRC) 
	g++ -Werror -Wall -g -o $(EXE) $(SRC)

agent: $(SRC) 
	g++ -Werror -Wall  -g -o $(EXE) $(SRC) 

coverage: $(SRC) 
	g++ -g --coverage -o $(EXE) $(SRC)
    
clean:
	rm -f $(EXE) 
	rm -f $(VALGRINDLOG)

memcheck: agent 
	rm -f $(VALGRINDLOG)
	valgrind --read-var-info=yes --track-origins=yes --leak-check=full --log-file=$(VALGRINDLOG)  ./$(EXE)
	cat $(VALGRINDLOG)
