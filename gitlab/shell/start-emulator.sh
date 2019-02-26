if [ ${START_EMU} = "1" ]; then
  export ANDROID_EMU_IMAGE="system-images;${ANDROID_EMU_TARGET};${ANDROID_EMU_TAG};${ANDROID_EMU_ABI}"
  for retry in 1 2 3; do
    echo yes | sdkmanager "${ANDROID_EMU_IMAGE}" > /dev/null && break
    echo "sdkmanager was not able to download the ${ANDROID_EMU_IMAGE} image (retry ${retry})"
    sleep 5
  done
  sdkmanager --list
  export TOOLS=${ANDROID_HOME}/tools
  export PATH=${ANDROID_HOME}:${ANDROID_HOME}/emulator:${TOOLS}:${TOOLS}/bin:${ANDROID_HOME}/platform-tools:${PATH}
  echo no | avdmanager create avd -k "${ANDROID_EMU_IMAGE}" -n "${ANDROID_EMU_NAME}" -f --abi "${ANDROID_EMU_ABI}" --tag "${ANDROID_EMU_TAG}" || exit 1
  emulator -avd "${ANDROID_EMU_NAME}" -no-window -camera-back none -camera-front none &
else
  sdkmanager --list
fi