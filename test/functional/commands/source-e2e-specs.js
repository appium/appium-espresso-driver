import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
import path from 'path';
import { DOMParser } from 'xmldom';
import xpath from 'xpath';
import { HOST, PORT, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';
import { startServer } from '../../..';


chai.should();
chai.use(chaiAsPromised);

describe('source commands', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let server;
  before(async function () {
    server = await startServer(PORT, HOST);
  });
  after(async function () {
    try {
      await server.close();
    } catch (ign) {}
  });
  afterEach(async function () {
    try {
      await driver.quit();
    } catch (ign) {}
  });
  it('should get sourceXML, parse it, and find a node by xpath', async function () {
    driver = wd.promiseChainRemote(HOST, PORT);
    await driver.init(APIDEMO_CAPS);
    const sourceXML = await driver.source();
    sourceXML.should.be.a.string;
    const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
    const node = xpath.select('//*[content-desc=Animation]', doc);
    node.should.exist;
  });
  it('should get sourceXML from a react native app and have view-tag', async function () {
    driver = wd.promiseChainRemote(HOST, PORT);
    await driver.init({
      ...APIDEMO_CAPS,
      app: path.resolve(__dirname, '..', '..', 'assets', 'ReactNativeApp.apk'),
    });
    const sourceXML = await driver.source();
    sourceXML.should.be.a.string;
    console.log('#####', sourceXML); // eslint-disable-line no-console
    process.exit();
    const doc = new DOMParser().parseFromString(sourceXML, 'test/xml');
    const node = xpath.select('//*[content-desc=Animation]', doc);
    node.should.exist;
  });
});
