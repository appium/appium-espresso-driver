import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('compose node attributes', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
    if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 23) {
      return this.skip();
    }
    driver = await initSession(COMPOSE_CAPS);
  });
  after(async function () {
    await deleteSession();
  });
  describe('compose getAttribute', function () {
    it(`should get the 'content-desc' of a View`, async function () {
      let el = await driver.elementByXPath("//*[@text='Clickable Component']");
      await driver.moveTo(el);
      await el.click();

      await driver.updateSettings({ driver: 'compose' });

      let taggedElement = await driver.elementByTagName('lol');
      await taggedElement.getAttribute('view-tag').should.eventually.equal('lol');
    });
    it(`should get the 'text' of a View`, async function () {
      let el = await driver.elementByLinkText('Click to see dialog');
      await el.getAttribute('text').should.eventually.equal('Click to see dialog');

      const selected = el.getAttribute('selected');
      await el.isSelected().should.eventually.equal(selected);
      // el.getAttribute('class');
      // el.getAttribute('clickable');
      // el.getAttribute('content-desc');
      // el.getAttribute('enabled');
      // el.getAttribute('focused');
      // el.getAttribute('text');
    });
  });
});
