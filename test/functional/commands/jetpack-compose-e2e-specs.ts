import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {COMPOSE_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('Jetpack Compose', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver: any;

  before(async function () {
    // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
    if (parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 23) {
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

    await driver.updateSettings({driver: 'compose'});

    const e = await driver.$(await driver.findElement('tag name', 'lol'));
    await expect(driver.isElementDisplayed(e.elementId)).to.eventually.be.true;

    const elementWithDescription = await driver.$('~desc');
    await expect(elementWithDescription.getText()).to.eventually.equal('Click to see dialog');
    await expect(driver.isElementDisplayed(elementWithDescription.elementId)).to.eventually.be.true;

    const clickableText = await driver.$('=Click to see dialog');
    await clickableText.click();

    await driver.$('=Congratulations! You just clicked the text successfully');
    await expect(driver.getSettings()).to.eventually.eql({driver: 'compose'});
  });

  it('should find element by xpath', async function () {
    await driver.updateSettings({driver: 'espresso'});
    const el = await driver.$("//*[@text='Clickable Component']");
    await el.click();

    await driver.updateSettings({driver: 'compose'});

    const e = await driver.$("//*[@view-tag='lol']//*[@content-desc='desc']");
    await expect(e.getText()).to.eventually.equal('Click to see dialog');
  });

  it('should find elements', async function () {
    await driver.updateSettings({driver: 'espresso'});
    const el = await driver.$("//*[@text='Horizontal Carousel']");
    await el.click();

    await driver.updateSettings({driver: 'compose'});

    const e = await driver.$$('=Grace Hopper');
    expect(e.length).to.eql(2);
    await expect(e[0].getText()).to.eventually.equal('Grace Hopper');
  });
});
