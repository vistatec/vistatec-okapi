#!/bin/bash
cd "$(dirname "$0")"
java -d32 -XstartOnFirstThread -Xdock:name="Rainbow" -jar ../../../lib/rainbow.jar
