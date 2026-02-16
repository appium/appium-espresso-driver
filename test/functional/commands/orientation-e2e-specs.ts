import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('orientation', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get orientation', async function () {
    await expect(driver.getOrientation()).to.eventually.eql('PORTRAIT');

    await driver.setOrientation('landscape');
    await expect(driver.getOrientation()).to.eventually.eql('LANDSCAPE');

    await driver.setOrientation('PORTRAIT');
    await expect(driver.getOrientation()).to.eventually.eql('PORTRAIT');
  });
});
