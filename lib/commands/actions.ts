import type {EspressoDriver} from '../driver';
import type {StringRecord} from '@appium/types';

/**
 * Performs a sequence of W3C actions (e.g., pointer, key, wheel actions).
 * Automatically converts pointer actions to touch type for mobile compatibility.
 * @param actions - Array of action objects following the W3C Actions protocol
 * @returns Promise that resolves when all actions are performed
 */
export async function performActions(this: EspressoDriver, actions: StringRecord[]): Promise<void> {
  this.log.debug(`Received the following W3C actions: ${JSON.stringify(actions, null, '  ')}`);
  // This is needed because Selenium API uses MOUSE as the default pointer type
  const preprocessedActions = actions.map((action) => ({
    ...action,
    ...(action.type === 'pointer' ? {parameters: {pointerType: 'touch'}} : {}),
  }));
  this.log.debug(`Preprocessed actions: ${JSON.stringify(preprocessedActions, null, '  ')}`);
  await this.espresso.jwproxy.command('/actions', 'POST', {actions: preprocessedActions});
}
