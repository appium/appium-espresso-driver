import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import type {Browser} from 'webdriverio';

import {APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('Size', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should find rect of window', async function () {
    const {width, height, x, y} = await driver.getWindowRect();
    assert.ok(width > 0);
    assert.ok(height > 0);
    assert.strictEqual(x, 0);
    assert.strictEqual(y, 0);
  });

  it('should find rect of an element', async function () {
    const el = await driver.$('~App');
    const {width, height, x, y} = await driver.getElementRect(await el.elementId);
    assert.ok(width > 0);
    assert.ok(height > 0);
    // the element start from the edge of left.
    assert.strictEqual(x, 0);
    assert.ok(y > 0);
  });
});
