import assert from 'node:assert/strict';
import {describe, it, before, beforeEach, afterEach} from 'node:test';

import {util} from 'appium/support.js';
import {retryInterval} from 'asyncbox';
import axios from 'axios';
import type {Browser} from 'webdriverio';

import {type ComposeCaps, getComposeCaps} from '../desired.js';
import {initSession, deleteSession, HOST, PORT, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('Jetpack Compose W3C actions', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;
  let composeCaps: ComposeCaps;
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

  const tapElement = (elementId: string) =>
    driver.performActions([
      {
        type: 'pointer',
        id: 'finger',
        parameters: {pointerType: 'touch'},
        actions: [
          {type: 'pointerMove', duration: 0, x: 0, y: 0, origin: {[util.W3C_WEB_ELEMENT_IDENTIFIER]: elementId}},
          {type: 'pointerDown', button: 0},
          {type: 'pause', duration: 50},
          {type: 'pointerUp', button: 0},
        ],
      },
    ]);

  const openTextInput = async () => {
    const windowRect = await driver.getWindowRect();
    await retryInterval(10, 10_000, async () => {
      await driver.performActions([
        {
          type: 'pointer',
          id: 'touch',
          actions: [
            {
              type: 'pointerMove',
              duration: 50,
              x: windowRect.width / 2.0,
              y: windowRect.height / 2.0,
              origin: 'viewport',
            },
            {type: 'pointerDown', button: 0},
            {type: 'pause', duration: 500},
            {
              type: 'pointerMove',
              duration: 500,
              x: windowRect.width / 2.0,
              y: windowRect.height / 8.0,
              origin: 'viewport',
            },
            {type: 'pointerUp', button: 0},
          ],
        },
      ]);
      const el = await driver.$("//*[@text='Text Input Components']");
      await driver.elementClick(await el.elementId);
    });
    await driver.updateSettings({driver: 'compose'});
  };

  const getTextInput = async () => driver.$(await driver.findElement('tag name', 'text_input'));

  before(async function () {
    composeCaps = await getComposeCaps();
  });

  beforeEach(async function () {
    driver = await initSession(composeCaps);
  });

  afterEach(async function () {
    await deleteSession();
  });

  it('should tap a compose element using the element origin', async function () {
    const el = await driver.$("//*[@text='Clickable Component']");
    await el.click();
    await driver.updateSettings({driver: 'compose'});

    const clickable = await driver.$('=Click to see dialog');
    await tapElement(await clickable.elementId);

    await (
      await driver.$('=Congratulations! You just clicked the text successfully')
    ).waitForExist({
      timeout: 5000,
    });
  });

  it('should tap an Espresso view using the element origin in compose mode', async function () {
    const el = await driver.$("//*[@text='Clickable Component']");
    const elementId = await el.elementId;
    await driver.updateSettings({driver: 'compose'});

    await tapElement(elementId);

    await (await driver.$('=Click to see dialog')).waitForExist({timeout: 5000});
  });

  it('should type ASCII and non-ASCII keys into the focused text field', async function () {
    await openTextInput();
    const textInput = await getTextInput();
    // Focuses the field without a pointer tap
    await driver.setValueImmediate(await textInput.elementId, '!');

    await typeEach('ab');
    await typeEach('Привіт');
    await typeEach('c');

    // The IME may move the caret or replace its composing word, so only check the typed run
    assert.ok((await textInput.getText()).includes('abПривітc'));
  });

  it('should type into the focused text field using the legacy keys endpoint', async function () {
    await openTextInput();
    const textInput = await getTextInput();
    await driver.setValueImmediate(await textInput.elementId, '!');

    const {status} = await axios.post(`http://${HOST}:${PORT}/session/${driver.sessionId}/keys`, {
      value: ['a', 'Ж', 'b'],
    });
    assert.strictEqual(status, 200);

    assert.ok((await textInput.getText()).includes('aЖb'));
  });

  it('should keep Compose usable after tapping a text field', async function () {
    await openTextInput();
    const textInput = await getTextInput();

    await tapElement(await textInput.elementId);
    await typeEach('ab');

    // The caret position and IME timing vary per device, so only check the original text survived
    assert.strictEqual((await textInput.getText()).replace(/[ab]/g, ''), 'Enter your text here');
  });
});
