import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('Size', function () {
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

  it('should find size of window', async function () {
    const {width, height} = await driver.getWindowSize();
    width.should.be.above(0);
    height.should.be.above(0);
  });
});
