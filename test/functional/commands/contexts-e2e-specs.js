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
    if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 25) {
      // Latest 25 and lower has no good chromedriver to automate.
      this.skip();
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
