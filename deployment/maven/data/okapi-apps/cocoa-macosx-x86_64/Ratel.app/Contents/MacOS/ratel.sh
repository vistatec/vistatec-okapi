#!/bin/bash

OKAPI_HOME="${OKAPI_HOME:-$(dirname "$0")/../../../}"

JAVA="$("$OKAPI_HOME/check_java.sh")"

if [ -z "$JAVA" ]; then
    exit 1
fi

exec "$JAVA" -d64 -XstartOnFirstThread -Xdock:name="Ratel" -jar "$OKAPI_HOME/lib/ratel.jar"
