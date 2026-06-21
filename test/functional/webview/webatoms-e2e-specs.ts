import {describe, it, before, after} from 'node:test';
import type {Browser} from 'webdriverio';
import {sleep} from 'asyncbox';
import {use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession} from '../helpers/session.js';
import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

describe('mobile web atoms', function () {
  let driver: Browser;

  before(async function (t) {
    // API level 26 emulators don't have WebView installed by default.
    if (process.env.CI && parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 26) {
      (t as any).skip();
    }

    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:appPackage': 'io.appium.android.apis',
        'appium:appActivity': 'io.appium.android.apis.view.WebView1',
      }),
    );
  });
  after(async function () {
    await deleteSession();
  });

  it('should input text into textbox and click links', async function () {
    const webviewEl = await driver.$(await driver.findElement('id', 'wv1'));
    await sleep(10000); // Wait for WebView to load
    await driver.execute(`mobile: webAtoms`, {
      webviewEl: webviewEl.elementId,
      forceJavascriptEnabled: true,
      methodChain: [
        {
          name: 'withElement',
          atom: {name: 'findElement', locator: {using: 'ID', value: 'i_am_a_textbox'}},
        },
        {name: 'perform', atom: {name: 'webKeys', args: 'Hello world'}},
        {
          name: 'withElement',
          atom: {name: 'findElement', locator: {using: 'ID', value: 'i am a link'}},
        },
        {name: 'perform', atom: 'webClick'},
      ],
    });
  });
});
