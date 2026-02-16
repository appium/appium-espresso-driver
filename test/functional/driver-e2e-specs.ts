import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {remote} from 'webdriverio';
import {COMMON_REMOTE_OPTIONS} from './helpers/session';
import {APIDEMO_CAPS, amendCapabilities} from './desired';

chai.use(chaiAsPromised);

describe('EspressoDriver', function () {
  let driver;

  before(async function () {});

  describe('createSession', function () {
    describe('success', function () {
      afterEach(async function () {
        try {
          await driver.deleteSession();
        } catch {}
        driver = null;
      });

      it('should start android session focusing on default activity', async function () {
        driver = await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: APIDEMO_CAPS,
        });
        await expect(driver.getCurrentActivity()).to.eventually.equal('.ApiDemos');
      });
      it('should start android session focusing on specified activity', async function () {
        // for now the activity needs to be fully qualified
        driver = await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: amendCapabilities(APIDEMO_CAPS, {
            'appium:appActivity':
              'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
          }),
        });
        await expect(driver.getCurrentActivity()).to.eventually.equal(
          '.accessibility.AccessibilityNodeProviderActivity',
        );
      });
    });
    describe('failure', function () {
      it('should reject start session for non-existent activity', async function () {
        // for now the activity needs to be fully qualified
        await expect(
          remote({
            ...COMMON_REMOTE_OPTIONS,
            capabilities: amendCapabilities(APIDEMO_CAPS, {
              'appium:appActivity': 'io.appium.android.apis.some.fake.Activity',
            }),
          }),
        ).to.be.rejected;
      });
      it('should reject opening of appPackage with incorrect signature', async function () {
        await expect(
          remote({
            ...COMMON_REMOTE_OPTIONS,
            capabilities: amendCapabilities(APIDEMO_CAPS, {
              'appium:appActivity': 'com.android.settings',
            }),
          }),
        ).to.be.rejected;
      });
    });
  });
  describe('.startActivity', function () {
    afterEach(async function () {
      try {
        await driver.deleteSession();
      } catch {}
      driver = null;
    });
    it('should start activity by name', async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: APIDEMO_CAPS,
      });
      await driver.startActivity(
        'io.appium.android.apis',
        '.accessibility.AccessibilityNodeProviderActivity',
      );
      await expect(driver.getCurrentActivity()).to.eventually.eql(
        '.accessibility.AccessibilityNodeProviderActivity',
      );
    });
    it('should start activity by fully-qualified name', async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: APIDEMO_CAPS,
      });
      await driver.startActivity(
        'io.appium.android.apis',
        'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
      );
      await expect(driver.getCurrentActivity()).to.eventually.eql(
        '.accessibility.AccessibilityNodeProviderActivity',
      );
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
      driver = null;
    });
    it('should send keys to focused-on element', async function () {
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
      await expect(editEl.getText()).to.eventually.equal('Hello World!');
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
        await expect(editEl.getText()).to.eventually.equal(isLongPress ? 'GG' : 'G');
        await editEl.clearValue();
      }
    });
  });
});
