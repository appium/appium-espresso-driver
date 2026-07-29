import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import {DOMParser} from '@xmldom/xmldom';
import type {Browser} from 'webdriverio';
import xpath from 'xpath';

import {APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('source commands', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  describe('regular app', function () {
    before(async function () {
      driver = await initSession(APIDEMO_CAPS);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get sourceXML, parse it, and find a node by xpath', async function () {
      const sourceXML = await driver.getPageSource();
      assert.strictEqual(typeof sourceXML, 'string');
      const doc = new DOMParser().parseFromString(sourceXML, 'application/xml');
      const node = xpath.select('//*', doc as unknown as Node);
      assert.ok(node);
    });
  });
});
