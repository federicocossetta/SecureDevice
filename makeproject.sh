#!/bin/sh
BASE_PATH=$(cd `dirname $0` && pwd)
${BASE_PATH}/gradlew :secureDevice:assembleRelease
cp -ivr "${BASE_PATH}\securedevice\build\outputs\aar\securedevice-release.aar" "${BASE_PATH}\securedevice.aar"