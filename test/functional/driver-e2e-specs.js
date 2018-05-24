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
      appActivity: 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity'
    }, APIDEMO_CAPS));

    status[1].app.should.eql(APIDEMO_CAPS.app);

    let activity = await driver.getCurrentDeviceActivity();
    activity.should.equal('.accessibility.AccessibilityNodeProviderActivity');
  });
  it('should reject start session for non-existent activity', async function () {
    // for now the activity needs to be fully qualified
    await driver.init(Object.assign({
      appActivity: 'io.appium.android.apis.some.fake.Activity'
    }, APIDEMO_CAPS)).should.eventually.be.rejectedWith(/unable to resolve/i);
  });
  it('should reject opening of appPackage with incorrect signature', async function () {
    await driver.init(Object.assign({
      appPackage: 'com.android.settings'
    }, APIDEMO_CAPS)).should.eventually.be.rejectedWith(/does not have a signature matching/i);
  });
  describe('.startActivity', function () {
    it('should start activity by name', async function () {
      await driver.init(APIDEMO_CAPS);
      await driver.startActivity({appActivity: '.accessibility.AccessibilityNodeProviderActivity'});
      await driver.getCurrentDeviceActivity().should.eventually.eql('.accessibility.AccessibilityNodeProviderActivity');
    });
    it('should start activity by fully-qualified name', async function () {
      await driver.init(APIDEMO_CAPS);
      await driver.startActivity({appActivity: 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity'});
      await driver.getCurrentDeviceActivity().should.eventually.eql('.accessibility.AccessibilityNodeProviderActivity');
    });
  });
});
