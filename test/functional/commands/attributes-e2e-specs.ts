import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {APIDEMO_CAPS} from '../desired';

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
      const el = await driver.$("//*[@text='Animation']");
      await expect(el.getAttribute('content-desc')).to.eventually.equal('Animation');
    });
    it(`should get the 'text' of a View`, async function () {
      const el = await driver.$("//*[@text='Animation']");
      await expect(el.getAttribute('text')).to.eventually.equal('Animation');
    });
    it('should not work if getting an attribute that does not exist', async function () {
      const el = await driver.$("//*[@text='Animation']");
      await expect(el.getAttribute('some-fake-property')).to.be.rejectedWith(
        /Attribute name should be one of/,
      );
    });
  });
});
