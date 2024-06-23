import { DOMParser } from '@xmldom/xmldom';
import xpath from 'xpath';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  describe('regular app', function () {
    before(async function () {
      driver = await initSession(APIDEMO_CAPS);

      chai = await import('chai');
      const chaiAsPromised = await import('chai-as-promised');

      chai.should();
      chai.use(chaiAsPromised.default);
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
