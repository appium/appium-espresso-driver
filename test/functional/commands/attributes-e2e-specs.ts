import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import type {Browser} from 'webdriverio';

import {APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('element attributes', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });
  describe('getAttribute', function () {
    it(`should get the 'content-desc' of a View`, async function () {
      const el = await driver.$("//*[@text='Animation']");
      assert.strictEqual(await el.getAttribute('content-desc'), 'Animation');
    });
    it(`should get the 'text' of a View`, async function () {
      const el = await driver.$("//*[@text='Animation']");
      assert.strictEqual(await el.getAttribute('text'), 'Animation');
    });
    it('should not work if getting an attribute that does not exist', async function () {
      const el = await driver.$("//*[@text='Animation']");
      await assert.rejects(el.getAttribute('some-fake-property'), /Attribute name should be one of/);
    });
  });
});
