import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
import { HOST, PORT, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';
import { startServer } from '../../..';


chai.should();
chai.use(chaiAsPromised);

describe('text', function () {
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

  it('should find an element by it\'s xpath', async () => {
    let el = await driver.elementByXPath("//*[@content-desc='Animation']");
    let text = await el.text();
    text.should.equal('Animation');
    el.should.exist;
    await el.click();
  });
});
