import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';


describe('Jetpack Compose', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
    if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 23) {
      this.skip();
    }
  });

  beforeEach(async function () {
    driver = await initSession(COMPOSE_CAPS);
  });

  afterEach(async function () {
    await deleteSession();
  });

  it('should find element by tag and text and click it', async function () {
    let el = await driver.elementByXPath("//*[@text='Clickable Component']");
    await driver.moveTo(el);
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    let e = await driver.elementByTagName('lol');
    await e.isDisplayed().should.eventually.be.true;

    let elementWithDescription = await driver.elementByAccessibilityId('desc');
    await elementWithDescription.text().should.eventually.equal('Click to see dialog');
    await elementWithDescription.isDisplayed().should.eventually.be.true;

    let clickableText = await driver.elementByLinkText('Click to see dialog');
    await clickableText.click();

    await driver.elementByLinkText('Congratulations! You just clicked the text successfully');
    await driver.settings().should.eventually.eql({ driver: 'compose' });

  });

  it('should find element by xpath', async function () {
    await driver.updateSettings({ driver: 'espresso' });
    let el = await driver.elementByXPath("//*[@text='Clickable Component']");
    await driver.moveTo(el);
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    let e = await driver.elementByXPath("//*[@view-tag='lol']//*[@content-desc='desc']");
    await e.text().should.eventually.equal('Click to see dialog');
  });

  it('should find elements', async function () {
    await driver.updateSettings({ driver: 'espresso' });
    let el = await driver.elementByXPath("//*[@text='Horizontal Carousel']");
    await driver.moveTo(el);
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    let e = await driver.elementsByLinkText('Grace Hopper');
    e.length.should.be.eql(2);
    await e[0].text().should.eventually.equal('Grace Hopper');
  });
});