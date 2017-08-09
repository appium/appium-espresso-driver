import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('moveTo', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async () => {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async () => {
    await deleteSession();
  });

  it('should move to an element', async () => {
    let el = await driver.elementByAccessibilityId('App');
    await el.click();
    let moveToEl = await driver.elementByAccessibilityId('Service');
    await driver.elementByAccessibilityId('Text Recognition').should.eventually.be.rejectedWith(/Could not find element/);
    await driver.moveTo(moveToEl);
    await driver.elementByAccessibilityId('Text-To-Speech').should.eventually.exist;
  });
});
