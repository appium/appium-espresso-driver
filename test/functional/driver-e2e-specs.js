import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
import { HOST, PORT } from './helpers/session';
import { APIDEMO_CAPS } from './desired';
import { startServer } from '../..';


chai.should();
chai.use(chaiAsPromised);

describe('createSession', function () {
  let driver;
  let server;
  before(async function () {
    server = await startServer(PORT, HOST);
    driver = wd.promiseChainRemote(HOST, PORT);
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

  it('should start android session focusing on default activity', async function () {
    let status = await driver.init(APIDEMO_CAPS);

    status[1].app.should.eql(APIDEMO_CAPS.app);

    let activity = await driver.getCurrentDeviceActivity();
    activity.should.equal('.ApiDemos');
  });
  it('should start android session focusing on specified activity', async function () {
    // for now the activity needs to be fully qualified
    let status = await driver.init(Object.assign({
      appActivity: 'io.appium.android.apis.view.TextFields'
    }, APIDEMO_CAPS));

    status[1].app.should.eql(APIDEMO_CAPS.app);

    let activity = await driver.getCurrentDeviceActivity();
    activity.should.equal('.view.TextFields');
  });
});
