import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import {DOMParser} from '@xmldom/xmldom';
import xpath from 'xpath';

import {type ComposeCaps, getComposeCaps} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('source commands', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;
  let composeCaps: ComposeCaps;

  describe('jetpack-compose app', function () {
    before(async function () {
      composeCaps = await getComposeCaps();
      driver = await initSession(composeCaps);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get jetpack-compose sourceXML, parse it, and find a node by xpath', async function () {
      const el = await driver.$("//*[@text='Display Text']");
      await el.click();
      await driver.updateSettings({driver: 'compose'});
      const sourceXML = await driver.getPageSource();
      assert.strictEqual(typeof sourceXML, 'string');
      const doc = new DOMParser().parseFromString(sourceXML, 'application/xml');
      const node = xpath.select('//*', doc as unknown as Node);
      assert.ok(node);
    });
  });
});
