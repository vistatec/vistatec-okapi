cd ../../
call mvn clean
if ERRORLEVEL 1 goto end

call mvn install -DskipITs
if ERRORLEVEL 1 goto end

cd deployment/maven
call ant
if ERRORLEVEL 1 goto end

cd ../../applications/integration-tests
call mvn clean verify
pause

:end
pause
