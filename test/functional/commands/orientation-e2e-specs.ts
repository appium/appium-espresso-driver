import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import type {Browser} from 'webdriverio';

import {APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('orientation', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get orientation', async function () {
    assert.deepStrictEqual(await driver.getOrientation(), 'PORTRAIT');

    await driver.setOrientation('landscape');
    assert.deepStrictEqual(await driver.getOrientation(), 'LANDSCAPE');

    await driver.setOrientation('PORTRAIT');
    assert.deepStrictEqual(await driver.getOrientation(), 'PORTRAIT');
  });
});
