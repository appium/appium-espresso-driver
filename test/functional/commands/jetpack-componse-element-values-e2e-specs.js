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
    let el = await driver.elementByXPath("//*[@text='Text Input Components']");
    await driver.moveTo(el);
    await el.click();

    await driver.updateSettings({ driver: 'compose' });

    let textElement = await driver.elementByTagName('text_input');
    // verify default text
    await textElement.text().should.eventually.equal('Enter your text here');

    await textElement.setImmediateValue(['hello']);
    // should append to the exiting text
    await driver.elementByTagName('text_input').text().should.eventually.equal('Enter your text herehello');

    textElement.setText(['テスト']);
    //  should replace existing text
    await textElement.text().should.eventually.equal('テスト');

    textElement.clear();
    //  should clear existing text
    await textElement.text().should.eventually.equal('');
  });
});
