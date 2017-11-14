REM The batch file copies the its2.0 directory into the \test\resources directory
REM The copy_test_suite.bat expects the ITS-2.0-Testsuite repository at the same level as the okapi repository trunk. 
REM Example (okapi-html5 is an example name but can be any valid name)
REM c:\top_dir\
REM c:\top_dir\okapi-html5
REM c:\top_dir\ITS-2.0-Testsuite
REM Note: Please make sure to name the Testsuite repository ITS-2.0-Testsuite or update the batch file to your custom name.

XCOPY %~dp0..\..\..\..\..\..\..\ITS-2.0-Testsuite\its2.0\expected\* %~dp0\its2.0\expected /s /i /y
XCOPY %~dp0..\..\..\..\..\..\..\ITS-2.0-Testsuite\its2.0\inputdata\* %~dp0\its2.0\inputdata /s /i /y