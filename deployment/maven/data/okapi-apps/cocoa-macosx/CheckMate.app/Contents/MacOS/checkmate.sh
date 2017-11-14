#!/bin/bash
cd "$(dirname "$0")"
java -d32 -XstartOnFirstThread -Xdock:name="CheckMate" -jar ../../../lib/checkmate.jar
