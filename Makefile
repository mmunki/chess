# ============================================================================
# chess -- makefile
# ----------------------------------------------------------------------------
# The game is written in plain Java and needs nothing but a JDK (17 or newer).
#
#   make                build dist/chess.jar
#   make test           build and run the checks
#   make run            start the game
#   make clean          remove the build artefacts
# ============================================================================
JAVAC ?= javac
JAVA  ?= java
JAR   ?= jar

OUT      := build/classes
TEST_OUT := build/test-classes
DIST     := dist
JARFILE  := $(DIST)/chess.jar
MAIN     := com.mmunki.chess.Main

.PHONY: all test run clean

all: $(JARFILE)

$(JARFILE): $(shell find src/main/java -name '*.java')
	@mkdir -p $(OUT) $(DIST)
	$(JAVAC) -Xlint:all -d $(OUT) $(shell find src/main/java -name '*.java')
	$(JAR) --create --file $(JARFILE) --main-class $(MAIN) -C $(OUT) .

test: $(JARFILE)
	@mkdir -p $(TEST_OUT)
	$(JAVAC) -d $(TEST_OUT) -cp $(OUT) $(shell find src/test/java -name '*.java')
	$(JAVA) -cp $(OUT):$(TEST_OUT) com.mmunki.chess.test.EngineCheck
	$(JAVA) -cp $(OUT):$(TEST_OUT) com.mmunki.chess.test.EngineCheck2

run: $(JARFILE)
	$(JAVA) -jar $(JARFILE)

clean:
	rm -rf build $(DIST)
