import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { amendCapabilities, APIDEMO_CAPS } from '../desired';


describe('context', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    // API level 26 emulators don't have WebView installed by default.
    if (process.env.CI && parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 26) {
      this.skip();
    }

    driver = await initSession(amendCapabilities(
      APIDEMO_CAPS,
      {
        'appium:appActivity': 'io.appium.android.apis.view.WebView1'
      }
    ));
  });
  after(async function () {
    await deleteSession();
  });

  it('should get contexts and set them without errors', async function () {
    const viewContexts = await driver.getContexts();

    await driver.getContext().should.eventually.eql(viewContexts[0]);

    await driver.switchContext(viewContexts[1]);
    await driver.getContext().should.eventually.eql(viewContexts[1]);

    await driver.switchContext(viewContexts[0]);
    await driver.getContext().should.eventually.eql(viewContexts[0]);
  });
});
