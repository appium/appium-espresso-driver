import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);


describe('execute mobile', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.view.AutoComplete4'
    }, APIDEMO_CAPS);
    driver = await initSession(caps);
  });
  after(async function () {
    await deleteSession();
  });

  it('should request permissions without failing', async function () {
    await driver.execute('mobile: requestPermissions', {permissions: ['android.permission.INTERNET']});
  });
  it('should fail when requesting non-permissions', async function () {
    await driver.execute('mobile: requestPermissions', {permissions: 'NOT A REAL PERMISSION'}).should.eventually.be.rejectedWith(/Failed to grant permissions,/);
  });
});
