import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('moveTo @skip-ci', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should move to an element', async function () {
    let el = await driver.elementByAccessibilityId('Views');
    await el.click();
    let moveToEl = await driver.elementByAccessibilityId('Expandable Lists');
    await driver.elementByAccessibilityId('ImageView').should.eventually.be.rejectedWith(/Could not find element/);
    await driver.moveTo(moveToEl);
    await driver.elementByAccessibilityId('ImageView').should.eventually.exist;
  });
});
