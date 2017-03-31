#!/bin/bash
# Abort on Error
set -e

export PING_SLEEP=30s
export BUILD_OUTPUT=$TRAVIS_BUILD_DIR/.travis/build.out

touch $BUILD_OUTPUT

dump_output() {
   echo Tailing the last 500 lines of output:
   tail -500 $BUILD_OUTPUT  
}

# Set up a repeating loop to send some output to Travis.
bash -c "while true; do echo \$(date) - building ...; sleep $PING_SLEEP; done" &
PING_LOOP_PID=$!
