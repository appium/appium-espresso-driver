import assert from 'node:assert/strict';
import {describe, it, before, beforeEach, afterEach} from 'node:test';

import {remote, type Browser} from 'webdriverio';

import {APIDEMO_CAPS, amendCapabilities} from './desired.js';
import {isApi} from './helpers/env.js';
import {COMMON_REMOTE_OPTIONS} from './helpers/session.js';

describe('EspressoDriver', function () {
  let driver: Browser;

  before(async function () {});

  describe('createSession', function () {
    describe('success', function () {
      afterEach(async function () {
        try {
          if (driver) {
            await driver.deleteSession();
          }
        } catch {}
      });

      it('should start android session focusing on default activity', async function () {
        driver = await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: APIDEMO_CAPS,
        });
        assert.strictEqual(await driver.getCurrentActivity(), '.ApiDemos');
      });
      it('should start android session focusing on specified activity', async function () {
        // for now the activity needs to be fully qualified
        driver = await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: amendCapabilities(APIDEMO_CAPS, {
            'appium:appActivity': 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
          }),
        });
        assert.strictEqual(await driver.getCurrentActivity(), '.accessibility.AccessibilityNodeProviderActivity');
      });
    });
    describe('failure', function () {
      it('should reject start session for non-existent activity', async function () {
        // for now the activity needs to be fully qualified
        await assert.rejects(
          remote({
            ...COMMON_REMOTE_OPTIONS,
            capabilities: amendCapabilities(APIDEMO_CAPS, {
              'appium:appActivity': 'io.appium.android.apis.some.fake.Activity',
            }),
          }),
        );
      });
      it('should reject opening of appPackage with incorrect signature', async function () {
        await assert.rejects(
          remote({
            ...COMMON_REMOTE_OPTIONS,
            capabilities: amendCapabilities(APIDEMO_CAPS, {
              'appium:appActivity': 'com.android.settings',
            }),
          }),
        );
      });
    });
  });
  describe('.startActivity', function () {
    afterEach(async function () {
      try {
        await driver.deleteSession();
      } catch {}
    });
    it('should start activity by name', async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: APIDEMO_CAPS,
      });
      await driver.execute('mobile:startActivity', {
        appActivity: '.accessibility.AccessibilityNodeProviderActivity',
      });
      assert.strictEqual(await driver.getCurrentActivity(), '.accessibility.AccessibilityNodeProviderActivity');
    });
    it('should start activity by fully-qualified name', async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: APIDEMO_CAPS,
      });
      await driver.execute('mobile:startActivity', {
        appActivity: 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
      });
      assert.strictEqual(await driver.getCurrentActivity(), '.accessibility.AccessibilityNodeProviderActivity');
    });
  });

  describe('keys', function () {
    beforeEach(async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: amendCapabilities(APIDEMO_CAPS, {
          'appium:appActivity': 'io.appium.android.apis.view.AutoComplete1',
          'appium:autoGrantPermissions': true,
        }),
      });
    });
    afterEach(async function () {
      try {
        await driver.deleteSession();
      } catch {}
    });
    // The first characters get lost on the API 29 emulator (failed in 4 CI runs, never on other API levels)
    const skipFocusedKeys = isApi(29) && 'flaky focus/IME timing on the API 29 emulator';
    it('should send keys to focused-on element', {skip: skipFocusedKeys}, async function () {
      const text = 'Hello World!';
      await driver.performActions([
        {
          type: 'key',
          id: 'keyboard',
          actions: Array.from(text).flatMap((char) => [
            {type: 'keyDown', value: char},
            {type: 'keyUp', value: char},
          ]),
        },
      ]);
      const editEl = await driver.$('//android.widget.AutoCompleteTextView');
      assert.strictEqual(await editEl.getText(), 'Hello World!');
      await editEl.clearValue();
    });

    it('should do long press keycode', async function () {
      const KEYCODE_G = 35;
      const META_SHIFT_MASK = 193;

      for (const isLongPress of [true, false]) {
        await driver.execute('mobile: pressKey', {
          keycode: KEYCODE_G,
          metastate: 0 | META_SHIFT_MASK,
          isLongPress,
        });
        const editEl = await driver.$('//android.widget.AutoCompleteTextView');
        assert.strictEqual(await editEl.getText(), isLongPress ? 'GG' : 'G');
        await editEl.clearValue();
      }
    });
  });
});
