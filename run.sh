#!/usr/bin/env bash
# Starts the game from the built jar.
set -euo pipefail
cd "$(dirname "$0")"
if [ ! -f dist/chess.jar ]; then
    ./build.sh
fi
exec java -jar dist/chess.jar "$@"
