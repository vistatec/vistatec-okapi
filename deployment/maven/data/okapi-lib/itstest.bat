@echo off
rem
rem This batch file invokes a short commend-line program in the ITS library
rem that generates the test output for a given input XML and HTML5 document.
rem The output is in the format used for the conformance tests.
rem See https://github.com/finnle/ITS-2.0-Testsuite for details.
rem
java -cp lib/okapi-lib-@version@.jar org.w3c.its.Main %*