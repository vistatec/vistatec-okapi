#!/bin/bash -e

cd ../..
cd okapi-ui/swt/core-ui/
mvn -PCOCOA_64_SWT dependency:resolve
cd ../../..

mvn clean -q
mvn install -q -TC8 -DskipITs

cd deployment/maven
ant -f build_getVerProp.xml

ant -f build_okapi-lib.xml

ant -f build_okapi-apps.xml -Dplatform=cocoa-macosx-x86_64 -DcodesignId="$OKAPI_CODESIGN_ID"

ant -f build_okapi-plugins.xml

# HACK: We copied everything into a subdirectory for DMG packaging,
# now copy it all back out so that we can run integration tests
cp -a dist_cocoa-macosx-x86_64/*/* dist_cocoa-macosx-x86_64

chmod a+x dist_cocoa-macosx-x86_64/tikal.sh
chmod a+x dist_cocoa-macosx-x86_64/check_java.sh
chmod a+x dist_cocoa-macosx-x86_64//Rainbow.app/Contents/MacOS/rainbow.sh
chmod a+x dist_cocoa-macosx-x86_64/CheckMate.app/Contents/MacOS/checkmate.sh
chmod a+x dist_cocoa-macosx-x86_64/examples/build_examples.sh
chmod a+x dist_cocoa-macosx-x86_64/examples/run_examples.sh
chmod a+x dist_cocoa-macosx-x86_64/Ratel.app/Contents/MacOS/ratel.sh

# rainbow and tikal tests
cd ../../applications/integration-tests
mvn clean -q -TC8 verify

# build okapi SDK artifacts
#cd ../deployment
#mvn clean install -q -TC4 -U


