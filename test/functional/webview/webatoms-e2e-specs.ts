import {describe, it, before, after} from 'node:test';
import type {Browser} from 'webdriverio';
import {sleep} from 'asyncbox';
import {use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';
import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

const SKIP_WEB_ATOMS_TESTS =
  Boolean(process.env.CI) && parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 26;

describe('mobile web atoms', {skip: SKIP_WEB_ATOMS_TESTS, timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
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
