import { DOMParser } from '@xmldom/xmldom';
import xpath from 'xpath';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';


describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  describe('jetpack-compose app', function () {
    before(async function () {
      chai = await import('chai');
      const chaiAsPromised = await import('chai-as-promised');

      chai.should();
      chai.use(chaiAsPromised.default);

      // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
      if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 23) {
        return this.skip();
      }
      driver = await initSession(COMPOSE_CAPS);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get jetpack-compose sourceXML, parse it, and find a node by xpath', async function () {
      let el = await driver.elementByXPath("//*[@text='Display Text']");
      await driver.moveTo(el);
      await el.click();
      await driver.updateSettings({ driver: 'compose' });
      const sourceXML = await driver.source();
      sourceXML.should.be.a.string;
      const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
      const node = xpath.select("//*[text='This is the Learn Jetpack Compose By Example tutorial']", doc);
      node.should.exist;
    });
  });
});
