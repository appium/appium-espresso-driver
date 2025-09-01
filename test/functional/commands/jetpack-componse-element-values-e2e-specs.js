import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';
import { retryInterval } from 'asyncbox';


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
    const windowRect = await driver.getWindowRect();
    await retryInterval(10, 10_000, async () => {
      await driver.performActions([
        {
          type: 'pointer',
          id: 'touch',
          actions: [
            { type: 'pointerMove', duration: 50, x: windowRect.width / 2.0, y: windowRect.height / 2.0, origin: 'viewport' },
            { type: 'pointerDown', button: 0 },
            { type: 'pause', duration: 500 },
            { type: 'pointerMove', duration: 500, x: windowRect.width / 2.0, y: windowRect.height / 8.0, origin: 'viewport' },
            { type: 'pointerUp', button: 0 },
          ]
        }
      ]);
      const el = await driver.$("//*[@text='Text Input Components']");
      await driver.elementClick(el.elementId);
    });

    await driver.updateSettings({ driver: 'compose' });

    let textElement = await driver.$(await driver.findElement('tag name', 'text_input'));
    // verify default text
    await textElement.getText().should.eventually.equal('Enter your text here');

    await driver.setValueImmediate(textElement.elementId, 'hello');
    // should append to the exiting text
    await driver.$(await driver.findElement('tag name', 'text_input')).getText().should.eventually.equal('Enter your text herehello');

    await textElement.setValue('テスト');
    //  should replace existing text
    await textElement.getText().should.eventually.equal('テスト');

    await textElement.clearValue();
    //  should clear existing text
    await textElement.getText().should.eventually.equal('');
  });
});
