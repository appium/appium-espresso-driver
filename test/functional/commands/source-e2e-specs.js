import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { DOMParser } from 'xmldom';
import xpath from 'xpath';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  describe('regular app', function () {
    before(async function () {
      driver = await initSession(APIDEMO_CAPS);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get sourceXML, parse it, and find a node by xpath', async function () {
      const sourceXML = await driver.source();
      sourceXML.should.be.a.string;
      const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
      const node = xpath.select('//*[content-desc=Animation]', doc);
      node.should.exist;
    });
  });
});
