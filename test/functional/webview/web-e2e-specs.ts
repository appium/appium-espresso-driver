import type {Browser} from 'webdriverio';
import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {amendCapabilities, APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('web', function () {
  before(async function () {
    // No proper chromedrivers are available, or very flaky to run on CI.
    // Also, API level 26 emulators don't have WebView installed by default.
    if (process.env.CI && parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 26) {
      this.skip();
    }
  });

  describe('WebView', function () {
    this.timeout(MOCHA_TIMEOUT);

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
      await expect(driver.getTitle()).to.eventually.equal('I am a page title');
    });
    it('should find one native and one web context', async function () {
      const contexts = await driver.getContexts();
      expect(contexts.length).to.equal(2);
      expect(contexts[0]).to.match(/^native/i);
      expect(contexts[1]).to.match(/^webview/i);
    });
    it('should send text to html text inputs', async function () {
      if (process.env.CI && parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) > 31) {
        // chromedriver or engine side issue on emulators.
        // Please relax the condition if newer ones work.
        this.skip();
      }

      const html = await driver.getPageSource();
      expect(html).to.match(/Selenium/);
      // Chrome 83 must be W3C
      const textbox = await driver.$('#i_am_a_textbox');
      await textbox.clearValue();
      await textbox.addValue('Text contents');
      await expect(textbox.getAttribute('value')).to.eventually.equal('Text contents');
      await textbox.clearValue();
      await expect(textbox.getText()).to.eventually.equal('');
    });
    it('should navigate between webview pages', async function () {
      const anchorLink = await driver.$('[id="i am a link"]');
      await anchorLink.click();
      const bodyEl = await driver.$(await driver.findElement('tag name', 'body'));
      expect(bodyEl).to.exist;
      await driver.back();
      const el = await driver.$('[id="i am a link"]');
      expect(el).to.exist;
    });
    it('should be able to switch from webview back to native, navigate to a different webview and then switch back to web context', async function () {
      // Switch to webview
      let contexts = await driver.getContexts();
      await driver.switchContext(contexts[1]);
      await expect(driver.getTitle()).to.eventually.equal('I am a page title');

      // Switch to native and go to different activity
      await driver.switchContext(contexts[0]);
      await driver.execute('mobile:startActivity', {
        appPackage: 'io.appium.android.apis',
        appActivity: 'io.appium.android.apis.view.WebView3',
      });
      contexts = await driver.getContexts();
      const el = await driver.$(await driver.findElement('id', 'android:id/content'));
      expect(el).to.exist;

      // Switch to webview again
      await driver.switchContext(contexts[1]);
      await expect(driver.getTitle()).to.eventually.equal('I am a page title');
    });
  });
});
