#!/usr/bin/env bash
# ============================================================================
# build-apk.sh -- build the game for the phone
# ----------------------------------------------------------------------------
# The game for the phone is the same game: the rules, the computer and the
# rating are the code of the game on the computer, only the part that draws
# the game is its own.
#
#   ./build-apk.sh      builds app/build/outputs/apk/release/app-release.apk
#
# The apk is signed with the key of the debug build when no key is given, so
# that it can always be put on a phone.  A key of your own is used when its
# properties are in the command:
#
#   ./build-apk.sh -Pchess.keystore=/path/to/key.jks \
#       -Pchess.keystore.password=... \
#       -Pchess.key.alias=... \
#       -Pchess.key.password=...
# ============================================================================
set -euo pipefail

cd "$(dirname "$0")"

chmod +x gradlew
./gradlew assembleRelease "$@"

APK="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK" ]; then
    echo "error: the apk was not built" >&2
    exit 1
fi

echo "==> done: $APK"
