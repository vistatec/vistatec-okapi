#!/bin/bash -e

cd ../..
cd okapi-ui/swt/core-ui/
mvn -PWIN_SWT -PWIN_64_SWT  -PCOCOA_64_SWT -PLinux_x86_swt -PLinux_x86_64_swt dependency:resolve
cd ../../..

mvn clean -q
mvn install -U -q -TC16 -DskipITs

cd deployment/maven
ant -f build_getVerProp.xml

ant -f build_okapi-lib.xml

ant -f build_okapi-apps.xml -Dplatform=gtk2-linux-x86_64

ant -f build_okapi-plugins.xml

chmod a+x dist_gtk2-linux-x86_64/tikal.sh
chmod a+x dist_gtk2-linux-x86_64/rainbow.sh
chmod a+x dist_gtk2-linux-x86_64/ratel.sh
chmod a+x dist_gtk2-linux-x86_64/checkmate.sh

# rainbow and tikal tests
cd ../../applications/integration-tests
mvn clean -q -TC16 verify

# build okapi SDK artifacts
#cd ../deployment
#mvn clean install -q -TC4 -U


