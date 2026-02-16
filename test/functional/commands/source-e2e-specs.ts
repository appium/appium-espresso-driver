import type {Browser} from 'webdriverio';
import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {DOMParser} from '@xmldom/xmldom';
import xpath from 'xpath';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver: Browser;

  describe('regular app', function () {
    before(async function () {
      driver = await initSession(APIDEMO_CAPS);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get sourceXML, parse it, and find a node by xpath', async function () {
      const sourceXML = await driver.getPageSource();
      expect(sourceXML).to.be.a.string;
      const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
      const node = xpath.select('//*', doc);
      expect(node).to.exist;
    });
  });
});
