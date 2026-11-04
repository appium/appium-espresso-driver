import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import {util} from 'appium/support.js';
import {retryInterval} from 'asyncbox';
import axios from 'axios';
import type {Browser} from 'webdriverio';

import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, HOST, PORT, E2E_TEST_TIMEOUT} from '../helpers/session.js';

// The server built without Compose must start and keep serving Espresso commands
describe('compose support disabled', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;
  let keyId = 0;

  const typeEach = (text: string) =>
    driver.performActions([
      {
        type: 'key',
        id: `keyboard-${keyId++}`,
        actions: Array.from(text).flatMap((value) => [
          {type: 'keyDown', value},
          {type: 'keyUp', value},
        ]),
      },
    ]);

  const getField = async () => {
    const el = await driver.$('//android.widget.AutoCompleteTextView');
    await el.click();
    await driver.elementClear(await el.elementId);
    await el.click();
    return el;
  };

  before(async function () {
    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:autoGrantPermissions': true,
        'appium:appActivity': 'io.appium.android.apis.view.AutoComplete4',
        'appium:espressoBuildConfig': JSON.stringify({composeSupport: false}),
      }),
    );
  });

  after(async function () {
    await deleteSession();
  });

  // The IME may drop characters while the emulator is still busy after the session start
  const typeAndCheck = (expected: string, type: (el: Awaited<ReturnType<typeof getField>>) => Promise<unknown>) =>
    retryInterval(3, 1000, async () => {
      const el = await getField();
      await type(el);
      assert.strictEqual(await el.getText(), expected);
    });

  it('should send keys to an element', async function () {
    await typeAndCheck('hello', async (el) => driver.elementSendKeys(await el.elementId, 'hello'));
  });

  it('should type ASCII and non-ASCII characters using W3C actions', async function () {
    await typeAndCheck('aжb', () => typeEach('aжb'));
  });

  it('should type using the legacy keys endpoint', async function () {
    await typeAndCheck('Привіт', async () =>
      axios.post(`http://${HOST}:${PORT}/session/${await driver.sessionId}/keys`, {
        value: Array.from('Привіт'),
      }),
    );
  });

  it('should tap an element using the element origin', async function () {
    await typeAndCheck('ж', async (el) => {
      await driver.performActions([
        {
          type: 'pointer',
          id: 'finger',
          parameters: {pointerType: 'touch'},
          actions: [
            {
              type: 'pointerMove',
              duration: 0,
              x: 0,
              y: 0,
              origin: {[util.W3C_WEB_ELEMENT_IDENTIFIER]: await el.elementId},
            },
            {type: 'pointerDown', button: 0},
            {type: 'pause', duration: 50},
            {type: 'pointerUp', button: 0},
          ],
        },
      ]);
      await typeEach('ж');
    });
  });
});
