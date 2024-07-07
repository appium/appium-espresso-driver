import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('window', function () {
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
  it(`should get the size of a window`, async function () {
    let win = await driver.getWindowSize();
    win.width.should.be.above(0);
    win.height.should.be.above(0);
  });
});