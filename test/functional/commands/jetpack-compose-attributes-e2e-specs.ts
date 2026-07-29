import assert from 'node:assert/strict';
import {describe, it, before, beforeEach, afterEach} from 'node:test';

import {type ComposeCaps, getComposeCaps} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('compose node attributes', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;
  let composeCaps: ComposeCaps;

  before(async function () {
    composeCaps = await getComposeCaps();
  });

  describe('compose getAttribute', function () {
    beforeEach(async function () {
      driver = await initSession(composeCaps);
    });

    afterEach(async function () {
      await deleteSession();
    });

    it(`should get attributes of a View`, async function () {
      const el = await driver.$("//*[@text='Clickable Component']");
      await el.click();

      await driver.updateSettings({driver: 'compose'});

      const taggedElement = await driver.$('<lol>');
      assert.strictEqual(await taggedElement.getAttribute('view-tag'), 'lol');

      const click_dialog = await driver.$("//*[@text='Click to see dialog']");
      assert.strictEqual(await click_dialog.getAttribute('text'), 'Click to see dialog');
      assert.strictEqual(await click_dialog.getText(), 'Click to see dialog');

      assert.strictEqual(await click_dialog.getAttribute('selected'), 'false');
      assert.strictEqual(await click_dialog.isSelected(), false);

      assert.strictEqual(await click_dialog.isDisplayed(), true);

      assert.strictEqual(await click_dialog.getAttribute('class'), 'Text');

      assert.strictEqual(await click_dialog.getAttribute('clickable'), 'false');

      assert.strictEqual(await click_dialog.getAttribute('enabled'), 'true');
      assert.strictEqual(await click_dialog.isEnabled(), true);

      assert.strictEqual(await click_dialog.getAttribute('focused'), 'false');
    });
  });
});
