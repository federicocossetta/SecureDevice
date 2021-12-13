#!/bin/sh
BASE_PATH=$(cd `dirname $0` && pwd)
${BASE_PATH}/gradlew -w -s :secureDevice:clean :secureDevice:assembleRelease
rm -rf "${BASE_PATH}\securedevice.aar"
cp -ivr "${BASE_PATH}\securedevice\build\outputs\aar\securedevice-release.aar" "${BASE_PATH}\securedevice.aar"