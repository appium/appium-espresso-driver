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

    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.view.WebView1',
    }, APIDEMO_CAPS);
    driver = await initSession(caps);
  });
  after(async function () {
    await deleteSession();
  });

  it('should get contexts and set them without errors', async function () {
    if (process.env.ANDROID_SDK_VERSION === '23') {
      // Latest 23 emulator has chrome '44.0.2403' instead of '43.0.2357'
      return;
    }

    const viewContexts = await driver.contexts();

    await driver.currentContext().should.eventually.eql(viewContexts[0]);

    await driver.context(viewContexts[1]);
    await driver.currentContext().should.eventually.eql(viewContexts[1]);

    await driver.context(viewContexts[0]);
    await driver.currentContext().should.eventually.eql(viewContexts[0]);
  });
});
