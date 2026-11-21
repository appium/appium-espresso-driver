import type {Orientation, Rotation} from '@appium/types';

import type {EspressoDriver} from '../driver.js';

declare module '../driver.js' {
  interface EspressoDriver {
    getOrientation(...args: Parameters<typeof getOrientation>): ReturnType<typeof getOrientation>;
    setOrientation(...args: Parameters<typeof setOrientation>): ReturnType<typeof setOrientation>;
    getRotation(...args: Parameters<typeof getRotation>): ReturnType<typeof getRotation>;
    setRotation(...args: Parameters<typeof setRotation>): ReturnType<typeof setRotation>;
  }
}

/**
 * Gets the current device orientation.
 * @returns The current device orientation ('LANDSCAPE' or 'PORTRAIT').
 */
export async function getOrientation(this: EspressoDriver): Promise<Orientation> {
  return (await this.espresso.jwproxy.command(`/orientation`, 'GET', {})) as Orientation;
}

/**
 * Sets the device orientation.
 * @param orientation - The desired orientation ('LANDSCAPE' or 'PORTRAIT').
 */
export async function setOrientation(this: EspressoDriver, orientation: Orientation): Promise<void> {
  const normalizedOrientation = orientation.toUpperCase() as Orientation;
  await this.espresso.jwproxy.command(`/orientation`, 'POST', {
    orientation: normalizedOrientation,
  });
}

/**
 * Gets the current device rotation.
 * @returns Object containing the current device rotation in degrees on each axis
 */
export async function getRotation(this: EspressoDriver): Promise<Rotation> {
  return (await this.espresso.jwproxy.command(`/rotation`, 'GET', {})) as Rotation;
}

/**
 * Sets the device rotation.
 * @param x - The desired rotation in degrees on the X axis
 * @param y - The desired rotation in degrees on the Y axis
 * @param z - The desired rotation in degrees on the Z axis
 */
export async function setRotation(this: EspressoDriver, x: number, y: number, z: number): Promise<void> {
  await this.espresso.jwproxy.command(`/rotation`, 'POST', {x, y, z});
}
