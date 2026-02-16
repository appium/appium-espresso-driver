import type {Browser} from 'webdriverio';
import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {amendCapabilities, APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('context', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver: Browser;

  before(async function () {
    // API level 26 emulators don't have WebView installed by default.
    const sdkVersion = parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10);
    if (process.env.CI && sdkVersion <= 26) {
      this.skip();
    }

    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:appActivity': 'io.appium.android.apis.view.WebView1',
      }),
    );
  });
  after(async function () {
    await deleteSession();
  });

  it('should get contexts and set them without errors', async function () {
    const viewContexts = await driver.getContexts();

    await expect(driver.getContext()).to.eventually.eql(viewContexts[0]);

    await driver.switchContext(viewContexts[1]);
    await expect(driver.getContext()).to.eventually.eql(viewContexts[1]);

    await driver.switchContext(viewContexts[0]);
    await expect(driver.getContext()).to.eventually.eql(viewContexts[0]);
  });
});
