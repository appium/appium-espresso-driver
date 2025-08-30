import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('context', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should get contexts and set them without errors', async function () {
    if (process.env.ANDROID_SDK_VERSION === '23') {
      // Latest 23 emulator has chrome '44.0.2403' instead of '43.0.2357'
      return;
    }

    await driver.execute('mobile: startActivity', {appActivity: 'io.appium.android.apis.view.WebView1'});

    const viewContexts = await driver.getContexts();

    await driver.getContext().should.eventually.eql(viewContexts[0]);

    await driver.switchContext(viewContexts[1]);
    await driver.getContext().should.eventually.eql(viewContexts[1]);

    await driver.switchContext(viewContexts[0]);
    await driver.getContext().should.eventually.eql(viewContexts[0]);
  });
});
