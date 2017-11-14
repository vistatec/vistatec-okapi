call ant -f build_getVerProp.xml
if ERRORLEVEL 1 goto end

cd ../../../deployment/maven
call ant -f build_okapi-plugins.xml
if ERRORLEVEL 1 goto end

:end
pause
