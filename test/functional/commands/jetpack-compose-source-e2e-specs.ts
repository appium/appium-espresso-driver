import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {DOMParser} from '@xmldom/xmldom';
import xpath from 'xpath';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {COMPOSE_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver: any;

  describe('jetpack-compose app', function () {
    before(async function () {
      // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
      if (parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 23) {
        return this.skip();
      }
      driver = await initSession(COMPOSE_CAPS);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get jetpack-compose sourceXML, parse it, and find a node by xpath', async function () {
      const el = await driver.$("//*[@text='Display Text']");
      await el.click();
      await driver.updateSettings({driver: 'compose'});
      const sourceXML = await driver.getPageSource();
      expect(sourceXML).to.be.a.string;
      const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
      const node = xpath.select('//*', doc);
      expect(node).to.exist;
    });
  });
});
