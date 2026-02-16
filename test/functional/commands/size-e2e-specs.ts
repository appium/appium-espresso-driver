import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('Size', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should find rect of window', async function () {
    const {width, height, x, y} = await driver.getWindowRect();
    expect(width).to.be.above(0);
    expect(height).to.be.above(0);
    expect(x).to.equal(0);
    expect(y).to.equal(0);
  });

  it('should find rect of an element', async function () {
    const el = await driver.$('~App');
    const {width, height, x, y} = await driver.getElementRect(el.elementId);
    expect(width).to.be.above(0);
    expect(height).to.be.above(0);
    // the element start from the edge of left.
    expect(x).to.equal(0);
    expect(y).to.be.above(0);
  });
});
