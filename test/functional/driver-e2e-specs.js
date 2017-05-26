import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
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

  it('should start android session focusing on default activity', async () => {
    let status = await driver.init(APIDEMO_CAPS);

    status[1].app.should.eql(APIDEMO_CAPS.app);

    let activity = await driver.getCurrentDeviceActivity();
    activity.should.equal('.ApiDemos');
  });
  it('should start android session focusing on specified activity', async () => {
    // for now the activity needs to be fully qualified
    let status = await driver.init(Object.assign({
      appActivity: 'io.appium.android.apis.view.TextFields'
    }, APIDEMO_CAPS));

    status[1].app.should.eql(APIDEMO_CAPS.app);

    let activity = await driver.getCurrentDeviceActivity();
    activity.should.equal('.view.TextFields');
  });
});
