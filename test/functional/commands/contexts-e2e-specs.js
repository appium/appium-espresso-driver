import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);


describe('context', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.view.WebView1',
    }, APIDEMO_CAPS);
    driver = await initSession(caps);
  });
  after(async function () {
    await deleteSession();
  });

  it('should get contexts and set them without errors', async function () {
    const viewContexts = await driver.contexts();

    await driver.currentContext().should.eventually.eql(viewContexts[0]);

    await driver.context(viewContexts[1]);
    await driver.currentContext().should.eventually.eql(viewContexts[1]);

    await driver.context(viewContexts[0]);
    await driver.currentContext().should.eventually.eql(viewContexts[0]);
  });
});
