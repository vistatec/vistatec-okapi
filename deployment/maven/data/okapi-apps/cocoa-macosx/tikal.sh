#!/bin/bash
java -d32 -XstartOnFirstThread -jar "`dirname $0`"/lib/tikal.jar "$@"
