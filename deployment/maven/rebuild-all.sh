#!/bin/bash
set -e

if [[ -e /usr/libexec/java_home ]]; then
    export JAVA_HOME=$(/usr/libexec/java_home -v 1.7)
fi

cd ../../
mvn clean

mvn install -DskipITs

cd deployment/maven
ant

cd ../../applications/integration-tests
mvn clean verify
echo
echo "Paused. Press Enter to continue."
read

