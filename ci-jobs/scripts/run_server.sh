export cwd=$(pwd)
pushd "$cwd"
cd ~
appium driver install --source=local "$cwd"
appium server \
    --port=$APPIUM_TEST_SERVER_PORT \
    --address=$APPIUM_TEST_SERVER_HOST \
    --relaxed-security \
    &
popd
secondsStarted=$(date +%s)
while ! nc -z $APPIUM_TEST_SERVER_HOST $APPIUM_TEST_SERVER_PORT; do
    sleep 0.1
    secondsElapsed=$(( $(date +%s) - secondsStarted ))
    if [[ $secondsElapsed -gt 30 ]]; then
    echo "Appium server was unable to start within 30 seconds timeout"
    exit 1
    fi
done