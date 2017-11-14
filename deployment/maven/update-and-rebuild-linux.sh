#!/bin/bash -e

cd ../..
mvn clean
mvn install -DskipITs

cd deployment/maven
ant -f build_getVerProp.xml

ant -f build_okapi-lib.xml

ant -f build_okapi-apps.xml -Dplatform=gtk2-linux-x86

ant -f build_okapi-plugins.xml

chmod a+x dist_gtk2-linux-x86/tikal.sh
chmod a+x dist_gtk2-linux-x86/rainbow.sh
chmod a+x dist_gtk2-linux-x86/ratel.sh
chmod a+x dist_gtk2-linux-x86/checkmate.sh

cd ../../applications/integration-tests
mvn clean verify
