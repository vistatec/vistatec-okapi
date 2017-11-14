#!/bin/bash -e

cd ../..
#mvn clean
#mvn install -DskipITs

cd deployment/maven
ant -f build_getVerProp.xml

ant -f build_okapi-lib.xml

ant -f build_okapi-apps.xml -Dplatform=win32-x86

ant -f build_okapi-plugins.xml

cd ../../applications/integration-tests
mvn clean verify
