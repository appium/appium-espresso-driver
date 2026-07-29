import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import type {Browser} from 'webdriverio';

import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

const SKIP_WEB_TESTS = Boolean(process.env.CI);

describe('web', {skip: SKIP_WEB_TESTS, timeout: E2E_TEST_TIMEOUT}, function () {
  describe('WebView', function () {
    let driver: Browser;
    before(async function () {
      driver = await initSession(
        amendCapabilities(APIDEMO_CAPS, {
          'appium:appPackage': 'io.appium.android.apis',
          'appium:appActivity': 'io.appium.android.apis.view.WebView1',
          'appium:autoWebview': true,
        }),
      );
    });
    after(async function () {
      await deleteSession();
    });
    it('should get the title of a webview page', async function () {
      assert.strictEqual(await driver.getTitle(), 'I am a page title');
    });
    it('should find one native and one web context', async function () {
      const contexts = await driver.getContexts();
      assert.strictEqual(contexts.length, 2);
      assert.match(contexts[0] as string, /^native/i);
      assert.match(contexts[1] as string, /^webview/i);
    });
    it('should send text to html text inputs', async function (t) {
      if (process.env.CI && parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) > 31) {
        // chromedriver or engine side issue on emulators.
        // Please relax the condition if newer ones work.
        return t.skip();
      }

      const html = await driver.getPageSource();
      assert.match(html, /Selenium/);
      // Chrome 83 must be W3C
      const textbox = await driver.$('#i_am_a_textbox');
      await textbox.clearValue();
      await textbox.addValue('Text contents');
      assert.strictEqual(await textbox.getAttribute('value'), 'Text contents');
      await textbox.clearValue();
      assert.strictEqual(await textbox.getText(), '');
    });
    it('should navigate between webview pages', async function () {
      const anchorLink = await driver.$('[id="i am a link"]');
      await anchorLink.click();
      const bodyEl = await driver.$(await driver.findElement('tag name', 'body'));
      assert.ok(bodyEl);
      await driver.back();
      const el = await driver.$('[id="i am a link"]');
      assert.ok(el);
    });
    it('should be able to switch from webview back to native, navigate to a different webview and then switch back to web context', async function () {
      // Switch to webview
      let contexts = await driver.getContexts();
      await driver.switchContext(contexts[1]);
      assert.strictEqual(await driver.getTitle(), 'I am a page title');

      // Switch to native and go to different activity
      await driver.switchContext(contexts[0]);
      await driver.execute('mobile:startActivity', {
        appPackage: 'io.appium.android.apis',
        appActivity: 'io.appium.android.apis.view.WebView3',
      });
      contexts = await driver.getContexts();
      const el = await driver.$(await driver.findElement('id', 'android:id/content'));
      assert.ok(el);

      // Switch to webview again
      await driver.switchContext(contexts[1]);
      assert.strictEqual(await driver.getTitle(), 'I am a page title');
    });
  });
});
