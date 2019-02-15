import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('element attributes', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });
  describe('getAttribute', function () {
    it(`should get the 'content-desc' of a View`, async function () {
      let el = await driver.elementByXPath("//*[@text='Animation']");
      await el.getAttribute('content-desc').should.eventually.equal('Animation');
    });
    it(`should get the 'text' of a View`, async function () {
      let el = await driver.elementByXPath("//*[@text='Animation']");
      await el.getAttribute('text').should.eventually.equal('Animation');
    });
    it('should not work if getting an attribute that does not exist', async function () {
      let el = await driver.elementByXPath("//*[@text='Animation']");
      await el.getAttribute('some-fake-property').should.eventually.be.rejectedWith(/Attribute name should be one of/);
    });
  });
});