import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
import sampleApps from 'sample-apps';
import { HOST, PORT, MOCHA_TIMEOUT } from './helpers/session';
import { startServer } from '../..';


chai.should();
chai.use(chaiAsPromised);

let defaultCaps = {
  androidInstallTimeout: 90000,
  app: sampleApps('ApiDemos-debug'),
  deviceName: 'Android',
  platformName: 'Android',
  forceEspressoRebuild: true,
};

describe('createSession', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let server;
  before(async () => {
    server = await startServer(PORT, HOST);
    driver = wd.promiseChainRemote(HOST, PORT);
  });
  afterEach(async () => {
    try {
      await driver.quit();
    } catch (ign) {}
    try {
      await server.close();
    } catch (ign) {}
  });
  it('should start android session focusing on default pkg and act', async () => {
    let status = await driver.init(defaultCaps);

    status[1].app.should.eql(defaultCaps.app);
  });
});
