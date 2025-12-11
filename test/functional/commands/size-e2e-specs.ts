// @ts-nocheck
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

  it('should find rect of window', async function () {
    const {width, height, x, y} = await driver.getWindowRect();
    width.should.be.above(0);
    height.should.be.above(0);
    x.should.eq(0);
    y.should.eq(0);
  });

  it('should find rect of an element', async function () {
    const el = await driver.$('~App');
    const {
      width,
      height,
      x,
      y
    } = await driver.getElementRect(el.elementId);
    width.should.be.above(0);
    height.should.be.above(0);
    // the element start from the edge of left.
    x.should.eq(0);
    y.should.be.above(0);
  });
});
