import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { DOMParser } from 'xmldom';
import xpath from 'xpath';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  describe('regular app', function () {
    before(async function () {
      driver = await initSession(COMPOSE_CAPS);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get sourceXML, parse it, and find a node by xpath', async function () {
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
