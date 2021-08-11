import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('Jetpack Compose', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
    if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 23) {
      return this.skip();
    }
    driver = await initSession(COMPOSE_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should find element by tag and text and click it', async function () {
    let el = await driver.elementByXPath("//*[@text='Clickable Component']");
    await driver.moveTo(el);
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    let e = await driver.elementByTagName('lol');
    await e.text().should.eventually.equal('Click to see dialog');

    let elementWithDescription = await driver.elementByAccessibilityId('desc');
    await elementWithDescription.text().should.eventually.equal('Click to see dialog');
    elementWithDescription.isDisplayed().should.eventually.be.true;

    let clickableText = await driver.elementByLinkText('Click to see dialog');
    clickableText.click();

    await driver.settings().should.eventually.eql({ driver: 'compose' });
  });
});