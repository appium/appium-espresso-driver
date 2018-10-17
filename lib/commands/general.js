let commands = {}, helpers = {}, extensions = {};


commands.mobilePerformEditorAction = async function (opts = {}) {
  const {action} = opts;
  return await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
};

commands.mobileSwipe = async function (opts = {}) {
  const {direction, elementId} = opts;
  return await this.espresso.jwproxy.command(`/appium/execute_mobile/${elementId}/swipe`, 'POST', {direction});
};

Object.assign(extensions, commands, helpers);
export { commands, helpers };
export default extensions;
