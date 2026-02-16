import _ from 'lodash';
import B from 'bluebird';
import type {EspressoDriver} from '../driver';
import type {ScreenshotsInfo} from './types';

// Display 4619827259835644672 (HWC display 0): port=0 pnpId=GGL displayName="EMU_display_0"
const DISPLAY_PATTERN = /^Display\s+(\d+)\s+\(.+display\s+(\d+)\).+displayName="([^"]*)/gm;

/**
 * Retrieves screenshots of each display available to Android.
 * This functionality is only supported since Android 10.
 *
 * @param displayId - Optional Android display identifier to take a screenshot for.
 * If not provided then screenshots of all displays are going to be returned.
 * If provided but no matches were found then an error is thrown.
 * @returns Promise that resolves to a dictionary of display screenshots, where keys are display IDs
 * and values contain display information and base64-encoded PNG screenshot data
 * @throws {Error} If display information cannot be determined or if a provided displayId is not found
 */
export async function mobileScreenshots(
  this: EspressoDriver,
  displayId?: number | string,
): Promise<ScreenshotsInfo> {
  const displaysInfo = await this.adb.shell(['dumpsys', 'SurfaceFlinger', '--display-id']);
  const infos: Record<string, {id: string; isDefault: boolean; name: string}> = {};
  let match;
  while ((match = DISPLAY_PATTERN.exec(displaysInfo))) {
    infos[match[1]] = {
      id: match[1],
      isDefault: match[2] === '0',
      name: match[3],
    };
  }
  if (_.isEmpty(infos)) {
    this.log.debug(displaysInfo);
    throw new Error('Cannot determine the information about connected Android displays');
  }
  this.log.info(`Parsed Android display infos: ${JSON.stringify(infos)}`);

  const toB64Screenshot = async (dispId: string): Promise<string> =>
    (await this.adb.takeScreenshot(dispId)).toString('base64');

  const displayIdStr = isNaN(Number(displayId)) ? null : `${displayId}`;
  if (displayIdStr) {
    if (!infos[displayIdStr]) {
      throw new Error(
        `The provided display identifier '${displayId}' is not known. ` +
          `Only the following displays have been detected: ${JSON.stringify(infos)}`,
      );
    }
    return {
      [displayIdStr]: {
        ...infos[displayIdStr],
        payload: await toB64Screenshot(displayIdStr),
      },
    };
  }

  const allInfos = _.values(infos);
  const screenshots = await B.all(allInfos.map(({id}) => toB64Screenshot(id)));
  const result: ScreenshotsInfo = {};
  for (const [info, payload] of _.zip(allInfos, screenshots)) {
    if (info && payload) {
      result[info.id] = {
        ...info,
        payload,
      };
    }
  }
  return result;
}

/**
 * Return the base 64 encoded screenshot data.
 * This method is called only when `appium:nativeWebScreenshot` is enabled
 * to avoid proxying requests to the chromedriver.
 * Without `appium:nativeWebScreenshot` or disabled, espresso driver
 * proxies screenshot endpoint requests to the espresso server directly.
 *
 * @returns {Promise<string>}
 */
export async function getScreenshot(this: EspressoDriver): Promise<string> {
  return String(await this.espresso.jwproxy.command('/screenshot', 'GET'));
}
