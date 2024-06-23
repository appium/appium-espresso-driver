import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS, GENERIC_CAPS } from '../desired';


describe('web', function () {

  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
  });

  describe('WebView', function () {
    this.timeout(MOCHA_TIMEOUT);

    let driver;
    before(async function () {
      driver = await initSession({
        ...APIDEMO_CAPS,
        appPackage: 'io.appium.android.apis',
        appActivity: 'io.appium.android.apis.view.WebView1',
        autoWebview: true,
        chromedriverUseSystemExecutable: true,
      });
    });
    after(async function () {
      await deleteSession();
    });
    it('should get the title of a webview page', async function () {
      await driver.title().should.eventually.equal('I am a page title');
    });
    it('should find one native and one web context', async function () {
      let contexts = await driver.contexts();
      contexts.length.should.equal(2);
      contexts[0].should.match(/^native/i);
      contexts[1].should.match(/^webview/i);
    });
    it('should send text to html text inputs', async function () {
      const html = await driver.source();
      html.should.match(/Selenium/);
      // Chrome 83 must be W3C
      const textbox = await driver.elementByCss('#i_am_a_textbox');
      await textbox.clear();
      await textbox.type('Text contents');
      await textbox.getAttribute('value').should.eventually.equal('Text contents');
      await textbox.clear();
      await textbox.text().should.eventually.equal('');
    });
    it('should navigate between webview pages', async function () {
      const anchorLink = await driver.elementByCss('[id="i am a link"]');
      await anchorLink.click();
      const bodyEl = await driver.elementByTagName('body');
      bodyEl.getAttribute('value').should.eventually.equal(/I am some other page content/);
      await driver.back();
      await driver.elementByCss('[id="i am a link"]').should.eventually.exist;
    });
    it('should be able to switch from webview back to native, navigate to a different webview and then switch back to web context', async function () {
      // Switch to webview
      let contexts = await driver.contexts();
      await driver.context(contexts[1]);
      await driver.title().should.eventually.equal('I am a page title');

      // Switch to native and go to different activity
      await driver.context(contexts[0]);
      await driver.startActivity({
        appPackage: 'io.appium.android.apis',
        appActivity: 'io.appium.android.apis.view.WebView3',
      });
      contexts = await driver.contexts();
      await driver.elementById('android:id/content').should.eventually.exist;

      // Switch to webview again
      await driver.context(contexts[1]);
      await driver.title().should.eventually.equal('I am a page title');
    });
  });

  describe('Chrome Browser', function () {
    it('should reject "browserName=Chrome" sessions', async function () {
      await initSession({
        ...GENERIC_CAPS,
        browserName: 'Chrome',
      }).should.eventually.be.rejectedWith(/doesn't have permission/);
      await deleteSession();
    });
  });

});
