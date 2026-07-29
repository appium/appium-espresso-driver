import assert from 'node:assert/strict';
import {describe, it, before, beforeEach, afterEach} from 'node:test';

import {retryInterval} from 'asyncbox';

import {type ComposeCaps, getComposeCaps} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('Jetpack Compose', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;
  let composeCaps: ComposeCaps;

  before(async function () {
    composeCaps = await getComposeCaps();
  });

  beforeEach(async function () {
    driver = await initSession(composeCaps);
  });

  afterEach(async function () {
    await deleteSession();
  });

  it('should find element by tag and text and click it', async function () {
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
      await driver.elementClick(el.elementId);
    });

    await driver.updateSettings({driver: 'compose'});

    const textElement = await driver.$(await driver.findElement('tag name', 'text_input'));
    // verify default text
    assert.strictEqual(await textElement.getText(), 'Enter your text here');

    await driver.setValueImmediate(textElement.elementId, 'hello');
    // should append to the exiting text
    assert.strictEqual(
      await driver.$(await driver.findElement('tag name', 'text_input')).getText(),
      'Enter your text herehello',
    );

    await textElement.setValue('テスト');
    //  should replace existing text
    assert.strictEqual(await textElement.getText(), 'テスト');

    await textElement.clearValue();
    //  should clear existing text
    assert.strictEqual(await textElement.getText(), '');
  });
});
