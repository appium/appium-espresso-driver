import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('elementByXPath', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });
  it(`should find an element by it's xpath`, async function () {
    let el = await driver.elementByXPath("//*[@text='Animation']");
    el.should.exist;
    await el.click();
    await driver.back();
  });
  it('should find multiple elements that match one xpath', async function () {
    let els = await driver.elementsByXPath('//android.widget.TextView');
    els.length.should.be.above(1);
    await els[0].click();
    await driver.back();
  });
  it('should get the first element of an xpath that matches more than one element', async function () {
    let el = await driver.elementByXPath('//android.widget.TextView');
    el.should.exist;
    await el.click();
    await driver.back();
  });
  it('should throw a stale element exception if clicking on element that does not exist', async function () {
    let el = await driver.elementByXPath("//*[@content-desc='Animation']");
    await el.click();
    await el.click().should.eventually.be.rejectedWith(/no longer exists /);
    await driver.back();
  });
  it('should get the isDisplayed attribute on the same element twice', async function () {
    let el = await driver.elementByXPath("//*[@content-desc='Animation']");
    await el.isDisplayed().should.eventually.be.true;
    await el.isDisplayed().should.eventually.be.true;
    await el.click();
    await driver.back();
  });
  it('should match an element if the element is off-screen but has an accessibility id', async function () {
    let el = await driver.elementByAccessibilityId('Views');
    await el.click();

    // Click on an element that is at the bottom of the list
    let moveToEl = await driver.elementByAccessibilityId('WebView');
    await moveToEl.click();
    await driver.back();
    await driver.back();
  });
  it('should test element equality', async function () {
    let el = await driver.elementByAccessibilityId('Views');
    let elAgain = await driver.elementByXPath("//*[@content-desc='Views']");
    let elNonMatch = await driver.elementByAccessibilityId('Preference');
    await el.equals(elAgain).should.eventually.be.true;
    await el.equals(elNonMatch).should.eventually.be.false;
  });
});
