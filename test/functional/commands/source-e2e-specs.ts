import type { Browser } from 'webdriverio';
import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { DOMParser } from '@xmldom/xmldom';
import xpath from 'xpath';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver: Browser;

  describe('regular app', function () {
    before(async function () {
      driver = await initSession(APIDEMO_CAPS);

      chai.should();
      chai.use(chaiAsPromised);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get sourceXML, parse it, and find a node by xpath', async function () {
      const sourceXML = await driver.getPageSource();
      sourceXML.should.be.a.string;
      const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
      const nodes = xpath.select('//*[content-desc=Animation]', doc) as Node[];
      nodes.length.should.be.greaterThan(0);
    });
  });
});
