let commands = {}, helpers = {}, extensions = {};


commands.mobilePerformEditorAction = async function (opts = {}) {
  const {action} = opts;
  return await this.espresso.jwproxy.command('/appium/device/perform_editor_action', 'POST', {action});
};


Object.assign(extensions, commands, helpers);
export { commands, helpers };
export default extensions;
