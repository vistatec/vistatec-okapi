#!/bin/bash
cd "$(dirname "$0")"
java -d32 -XstartOnFirstThread -Xdock:name="Ratel" -jar ../../../lib/ratel.jar
