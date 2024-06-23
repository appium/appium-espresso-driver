import path from 'path';
import { remote } from 'webdriverio';
import { HOST, PORT } from './helpers/session';
import { APIDEMO_CAPS, amendCapabilities } from './desired';


const COMMON_REMOTE_OPTIONS = {
  hostname: HOST,
  port: PORT,
};

describe('EspressoDriver', function () {
  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
  });

  describe('createSession', function () {
    describe('success', function () {
      afterEach(async function () {
        try {
          await driver.deleteSession();
        } catch (ign) {}
        driver = null;
      });

      it('should start android session focusing on default activity', async function () {
        driver = await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: APIDEMO_CAPS,
        });
        await driver.getCurrentActivity().should.eventually.equal('.ApiDemos');
      });
      it('should start android session focusing on specified activity', async function () {
        // for now the activity needs to be fully qualified
        driver = await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: amendCapabilities(APIDEMO_CAPS, {
            'appium:appActivity': 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity'
          }),
        });
        await driver.getCurrentActivity().should.eventually.equal('.accessibility.AccessibilityNodeProviderActivity');
      });
    });
    describe('failure', function () {
      it('should reject start session for non-existent activity', async function () {
        // for now the activity needs to be fully qualified
        await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: amendCapabilities(APIDEMO_CAPS, {
            'appium:appActivity': 'io.appium.android.apis.some.fake.Activity'
          }),
        }).should.eventually.be.rejected;
      });
      it('should reject opening of appPackage with incorrect signature', async function () {
        await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: amendCapabilities(APIDEMO_CAPS, {
            'appium:appActivity': 'com.android.settings'
          }),
        }).should.eventually.be.rejected;
      });
      it('should reject start session for internet permissions not set', async function () {
        // for now the activity needs to be fully qualified
        await remote({
          ...COMMON_REMOTE_OPTIONS,
          capabilities: amendCapabilities(APIDEMO_CAPS, {
            'appium:app': path.resolve('test', 'assets', 'ContactManager.apk')
          }),
        }).should.eventually.be.rejectedWith(/INTERNET/);
      });
    });
  });
  describe('.startActivity', function () {
    afterEach(async function () {
      try {
        await driver.deleteSession();
      } catch (ign) {}
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
      await driver.getCurrentActivity().should.eventually.eql('.accessibility.AccessibilityNodeProviderActivity');
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
      await driver.getCurrentActivity().should.eventually.eql('.accessibility.AccessibilityNodeProviderActivity');
    });
  });

  // TODO: Update tests for wdio compatibility
  // describe('keys', function () {
  //   beforeEach(async function () {
  //     driver = await remote({
  //       ...COMMON_REMOTE_OPTIONS,
  //       capabilities: amendCapabilities(APIDEMO_CAPS, {
  //         'appium:appActivity': 'io.appium.android.apis.view.AutoComplete1',
  //         'appium:autoGrantPermissions': true,
  //       })
  //     });
  //   });
  //   afterEach(async function () {
  //     try {
  //       await driver.deleteSession();
  //     } catch (ign) {}
  //     driver = null;
  //   });
  //   it('should send keys to focused-on element', async function () {
  //     await driver.keys('Hello World!'.split(''));
  //     const editEl = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
  //     await editEl.text().should.eventually.equal('Hello World!');
  //     await editEl.clear();
  //   });

  //   it('should do long press keycode', async function () {
  //     const KEYCODE_G = 35;
  //     const META_SHIFT_MASK = 193;
  //     let sessionId = await driver.getSessionId();

  //     const endpoints = ['long_press_keycode', 'press_keycode'];
  //     for (let endpoint of endpoints) {
  //       await axios({
  //         method: 'POST',
  //         url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/appium/device/${endpoint}`,
  //         data: {
  //           keycode: KEYCODE_G,
  //           metastate: 0 | META_SHIFT_MASK,
  //         },
  //       });
  //       const editEl = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
  //       await editEl.text().should.eventually.equal(endpoint === 'press_keycode' ? 'G' : 'GG');
  //       await editEl.clear();
  //     }
  //   });
  // });
});
