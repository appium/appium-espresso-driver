import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import _ from 'lodash';
import ADB from 'appium-adb';
import wd from 'wd';
import sampleApps from 'sample-apps'
import { startServer } from '../../..';


chai.should();
chai.use(chaiAsPromised);
let expect = chai.expect;

const HOST = '0.0.0.0',
      PORT = 4994;
const MOCHA_TIMEOUT = 60 * 1000 * (process.env.TRAVIS ? 8 : 4);

let defaultCaps = {
  androidInstallTimeout: 90000,
  app: sampleApps('ApiDemos-debug'),
  deviceName: 'Android',
  platformName: 'Android',
};

describe('createSession', function () {
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
  });
  it('should start android session focusing on default pkg and act', async () => {
    let status = await driver.init(defaultCaps);

    status[1].app.should.eql(defaultCaps.app);
  });
});
