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
    const el = await driver.$("//*[@text='Clickable Component']");
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    const e = await driver.$(await driver.findElement('tag name', 'lol'));
    await driver.isElementDisplayed(e.elementId).should.eventually.be.true;

    const elementWithDescription = await driver.$('~desc');
    await elementWithDescription.getText().should.eventually.equal('Click to see dialog');
    await driver.isElementDisplayed(elementWithDescription.elementId).should.eventually.be.true;

    const clickableText = await driver.$('=Click to see dialog');
    await clickableText.click();

    await driver.$('=Congratulations! You just clicked the text successfully');
    await driver.getSettings().should.eventually.eql({ driver: 'compose' });

  });

  it('should find element by xpath', async function () {
    await driver.updateSettings({ driver: 'espresso' });
    const el = await driver.$("//*[@text='Clickable Component']");
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    const e = await driver.$("//*[@view-tag='lol']//*[@content-desc='desc']");
    await e.getText().should.eventually.equal('Click to see dialog');
  });

  it('should find elements', async function () {
    await driver.updateSettings({ driver: 'espresso' });
    const el = await driver.$("//*[@text='Horizontal Carousel']");
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    const e = await driver.$$('=Grace Hopper');
    e.length.should.be.eql(2);
    await e[0].getText().should.eventually.equal('Grace Hopper');
  });
});