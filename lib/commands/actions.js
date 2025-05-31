/**
 * @this {import('../driver').EspressoDriver}
 * @param {import('@appium/types').StringRecord[]} actions
 * @returns {Promise<void>}
 */
export async function performActions(actions) {
  this.log.debug(`Received the following W3C actions: ${JSON.stringify(actions, null, '  ')}`);
  // This is needed because Selenium API uses MOUSE as the default pointer type
  const preprocessedActions = actions.map((action) => ({
    ...action,
    ...(action.type === 'pointer' ? {parameters: {pointerType: 'touch'}} : {})
  }));
  this.log.debug(`Preprocessed actions: ${JSON.stringify(preprocessedActions, null, '  ')}`);
  await this.espresso.jwproxy.command('/actions', 'POST', {actions: preprocessedActions});
}
