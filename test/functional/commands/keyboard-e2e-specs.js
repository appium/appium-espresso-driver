import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('createSession', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async () => {
    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.view.TextFields'
    }, APIDEMO_CAPS);
    driver = await initSession(caps);
  });
  after(async () => {
    await deleteSession();
  });

  it('should start android session focusing on default activity', async () => {
    let el = await driver.elementById('id/edit1');
    await el.click();

    try {
      await el.click();
    } catch (err) {
      console.log(err); // eslint-disable-line
    }

    await el.sendKeys('hello');
  });
});
