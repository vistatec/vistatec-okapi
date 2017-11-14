#!/bin/bash

java -cp .:../lib/okapi-lib-@version@.jar:example01/target/okapi-example-01-@version@.jar Main myFile.html -s pseudo myFile.out-pseudo.html
java -cp .:../lib/okapi-lib-@version@.jar:example01/target/okapi-example-01-@version@.jar Main myFile.html -s upper myFile.out-upper.html
java -cp .:../lib/okapi-lib-@version@.jar:example01/target/okapi-example-01-@version@.jar Main myFile.html -s pseudo -s upper myFile.out-both.html
java -cp .:../lib/okapi-lib-@version@.jar:example01/target/okapi-example-01-@version@.jar Main myFile.odt -s pseudo
java -cp .:../lib/okapi-lib-@version@.jar:example01/target/okapi-example-01-@version@.jar Main myFile.properties -s pseudo
java -cp .:../lib/okapi-lib-@version@.jar:example01/target/okapi-example-01-@version@.jar Main myFile.xml -s pseudo

java -cp .:../lib/okapi-lib-@version@.jar:example02/target/okapi-example-02-@version@.jar Main myFile.odt

java -cp .:../lib/okapi-lib-@version@.jar:example03/target/okapi-example-03-@version@.jar Main

java -cp .:../lib/okapi-lib-@version@.jar:example04/target/okapi-example-04-@version@.jar Main

java -cp .:../lib/okapi-lib-@version@.jar:example05/target/okapi-example-05-@version@.jar Main

