import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
import { DOMParser } from 'xmldom';
import xpath from 'xpath';
import { HOST, PORT, MOCHA_TIMEOUT } from './helpers/session';
import { APIDEMO_CAPS } from './desired';
import { startServer } from '../..';


chai.should();
chai.use(chaiAsPromised);

describe('createSession', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let server;
  before(async () => {
    server = await startServer(PORT, HOST);
    driver = wd.promiseChainRemote(HOST, PORT);
    await driver.init(APIDEMO_CAPS);
  });
  after(async () => {
    try {
      await server.close();
    } catch (ign) {}
  });
  afterEach(async () => {
    try {
      await driver.quit();
    } catch (ign) {}
  });
  it('should get sourceXML, parse it, and find a node by xpath', async () => {
    const sourceXML = await driver.source();
    sourceXML.should.be.a.string;
    const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
    const node = xpath.select('//*[content-desc=Animation]', doc);
    node.should.exist;
  });
});
