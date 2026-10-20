#!/usr/bin/env bash
# ============================================================================
# build.sh -- build the chess game
# ----------------------------------------------------------------------------
# The game is written in plain Java and uses nothing but the Java standard
# library, so a JDK is all that is needed:
#
#   ./build.sh          builds dist/chess.jar
#   ./build.sh test     builds and runs the checks
#   ./run.sh            starts the game
# ============================================================================
set -euo pipefail

cd "$(dirname "$0")"

JAVAC="${JAVAC:-javac}"
JAR="${JAR:-jar}"
OUT="build/classes"
TEST_OUT="build/test-classes"
DIST="dist"

if ! command -v "$JAVAC" >/dev/null 2>&1; then
    echo "error: no javac in the path, a JDK (17 or newer) is needed" >&2
    exit 1
fi

echo "==> compiling"
rm -rf "$OUT" "$TEST_OUT"
mkdir -p "$OUT" "$TEST_OUT" "$DIST"
find src/main/java -name '*.java' > build/sources.txt
"$JAVAC" -Xlint:all -d "$OUT" @build/sources.txt

echo "==> packaging"
"$JAR" --create --file "$DIST/chess.jar" --main-class com.mmunki.chess.Main -C "$OUT" .

if [ "${1:-}" = "test" ]; then
    echo "==> checking"
    find src/test/java -name '*.java' > build/test-sources.txt
    "$JAVAC" -d "$TEST_OUT" -cp "$OUT" @build/test-sources.txt
    java -cp "$OUT:$TEST_OUT" com.mmunki.chess.test.EngineCheck
    java -cp "$OUT:$TEST_OUT" com.mmunki.chess.test.EngineCheck2
fi

echo "==> done: $DIST/chess.jar"
