#!/usr/bin/env bash

# This script was copy-pasted from https://docs.microsoft.com/en-us/azure/devops/pipelines/languages/android?view=azure-devops#test-on-the-android-emulator
# with some changes

if [ $ANDROID_SDK_VERSION -eq 30 ]; then
  # API Level 30 does not have 'default' for now
  declare -r emulator="system-images;android-$ANDROID_SDK_VERSION;google_apis;x86"
else
  declare -r emulator="system-images;android-$ANDROID_SDK_VERSION;default;x86"
fi

declare -r ANDROID_AVD=test
echo "y" | $ANDROID_HOME/tools/bin/sdkmanager --install "$emulator"

# Create emulator
echo "no" | $ANDROID_HOME/tools/bin/avdmanager create avd -n $ANDROID_AVD -k "$emulator" --force

echo $ANDROID_HOME/emulator/emulator -list-avds

echo "Starting emulator"

if [ $ANDROID_SDK_VERSION -ge 28 ]; then
  nohup $ANDROID_HOME/emulator/emulator -avd $ANDROID_AVD -no-audio -accel auto -gpu auto -no-boot-anim -no-snapshot -delay-adb > /dev/null 2>&1 &

  echo "Waiting until emulator finishes its startup"
  secondsStarted=$(date +%s)
  $ANDROID_HOME/platform-tools/adb wait-for-device || exit 1
  bootDuration=$(( $(date +%s) - secondsStarted ))
  echo "Emulator booting took ${bootDuration}s"
else
  nohup $ANDROID_HOME/emulator/emulator -avd $ANDROID_AVD -no-audio -accel auto -gpu auto -no-boot-anim -no-snapshot > /dev/null 2>&1 &

  $ANDROID_HOME/platform-tools/adb wait-for-device get-serialno
  secondsStarted=$(date +%s)

  TIMEOUT=360
  while [[ $(( $(date +%s) - secondsStarted )) -lt $TIMEOUT ]]; do
    # Fail fast if Emulator process crashed
    pgrep -nf avd || exit 1

    processList=$(adb shell ps)
    if [[ "$processList" =~ "com.android.systemui" ]]; then
      echo "System UI process is running. Checking IME services availability"
      $ANDROID_HOME/platform-tools/adb shell ime list && break
    fi
    sleep 5
    secondsElapsed=$(( $(date +%s) - secondsStarted ))
    secondsLeft=$(( TIMEOUT - secondsElapsed ))
    echo "Waiting until emulator finishes services startup; ${secondsElapsed}s elapsed; ${secondsLeft}s left"
  done

  bootDuration=$(( $(date +%s) - secondsStarted ))
  if [[ $bootDuration -ge $TIMEOUT ]]; then
    echo "Emulator has failed to fully start within ${TIMEOUT}s"
    exit 1
  fi
  echo "Emulator booting took ${bootDuration}s"
fi

adb shell input keyevent 82
$ANDROID_HOME/platform-tools/adb devices
echo "Emulator started"
