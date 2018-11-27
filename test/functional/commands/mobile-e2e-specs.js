import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('mobile', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  describe('mobile:swipe', function () {
    it('should swipe up and swipe down', async function () {
      let el = await driver.elementByAccessibilityId('Views');
      await el.click();
      await driver.source().should.eventually.contain('Animation');
      let {value: element} = await driver.elementById('android:id/list');
      await driver.execute('mobile: swipe', {direction: 'up', element});
      await driver.source().should.eventually.contain('Spinner');
      await driver.execute('mobile: swipe', {direction: 'down', element});
      await driver.source().should.eventually.contain('Animation');
      await driver.back();
    });
  });

  describe('mobile: openDrawer, mobile: closeDrawer', function () {
    it('should call these two commands but fail because element is not a drawer', async function () {
      let el = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: openDrawer', {element: el, gravity: 1}).should.eventually.be.rejectedWith(/open drawer with gravity/);
      await driver.execute('mobile: closeDrawer', {element: el, gravity: 1}).should.eventually.be.rejectedWith(/close drawer with gravity/);
    });
  });
});
