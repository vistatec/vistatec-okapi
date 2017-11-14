@echo off

set inputFile=website_test_slim
set tempTmx=unknownSegments
set inputExt=html
set rnbArch=dist_win32-x86_64

echo This batch create a list a Trados segments
echo It is based on %inputFile%.%inputExt%
echo The file is named %inputFile%.tmx.txt
echo You must have build the Rainbow distribution
echo You must have installed the Trados plugins
echo This uses the %rnbArch% distribution

echo.
echo Running Trados Analysis Step...
java -jar ../../../../../../deployment/maven/%rnbArch%/lib/rainbow.jar -np -pln createGFTCPart1.pln %inputFile%.%inputExt% -log _createGFTC1Log.txt

echo Running Conversion Step...
java -jar ../../../../../../deployment/maven/%rnbArch%/lib/rainbow.jar -np -pln createGFTCPart2.pln %tempTmx%.tmx -log _createGFTC2Log.txt

echo Removing trailing tab characters...
java -jar ../../../../../../deployment/maven/%rnbArch%/lib/rainbow.jar -np -pln createGFTCPart3.pln %tempTmx%.tmx.txt -o %inputFile%.tmx.txt -log _createGFTC3Log.txt

echo Removing temporary files...
del %tempTmx%.tmx*
del _log.csv
del _log.txt
REM del _*Log.txt

echo.
pause