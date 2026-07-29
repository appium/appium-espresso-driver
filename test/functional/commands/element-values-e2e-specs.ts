import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import type {Browser} from 'webdriverio';

import {APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('ElementValue', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set value and replace them', async function () {
    await driver.$('~App').click();
    await driver.$('~Activity').click();
    await driver.$('~Custom Title').click();

    const el = await driver.$(await driver.findElement('class name', 'android.widget.EditText'));
    await driver.setValueImmediate(await el.elementId, 'hello');
    assert.strictEqual(await el.getText(), 'Left is besthello');
    await el.setValue('テスト');
    assert.strictEqual(await el.getText(), 'テスト');
  });
});
