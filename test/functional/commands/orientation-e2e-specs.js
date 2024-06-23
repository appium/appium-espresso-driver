import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('orientation', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);

    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get orientation', async function () {
    await driver.getOrientation().should.eventually.eql('PORTRAIT');

    await driver.setOrientation('landscape');
    await driver.getOrientation().should.eventually.eql('LANDSCAPE');

    await driver.setOrientation('PORTRAIT');
    await driver.getOrientation().should.eventually.eql('PORTRAIT');
  });
});
