#!/bin/bash

OKAPI_HOME="${OKAPI_HOME:-$(dirname "$0")}"

JAVA_HOME="$(/usr/libexec/java_home -v 1.7+ -F)"

if [ -z "$JAVA_HOME" ]; then
    echo "Okapi requires Java 1.7 or higher."
    exit 1
fi

JAVA="$JAVA_HOME/bin/java"

exec "$JAVA" -d64 -XstartOnFirstThread -jar "$OKAPI_HOME/lib/tikal.jar" "$@"
