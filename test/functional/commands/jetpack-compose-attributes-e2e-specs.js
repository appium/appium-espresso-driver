import { remote } from 'webdriverio';
import { MOCHA_TIMEOUT, HOST, PORT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';


const COMMON_REMOTE_OPTIONS = {
  hostname: HOST,
  port: PORT,
};

describe('compose node attributes', function () {
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
      return this.skip();
    }
  });

  describe('compose getAttribute', function () {
    beforeEach(async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: COMPOSE_CAPS,
      });
    });

    afterEach(async function () {
      try {
        await driver.deleteSession();
      } catch (ign) {}
      driver = null;
    });

    it(`should get attributes of a View`, async function () {
      const el = await driver.$("//*[@text='Clickable Component']");
      await el.click();

      await driver.updateSettings({ driver: 'compose' });

      const taggedElement = await driver.$('<lol>');
      await taggedElement.getAttribute('view-tag').should.eventually.equal('lol');

      const click_dialog = await driver.$("//*[@text='Click to see dialog']");
      await click_dialog.getAttribute('text').should.eventually.equal('Click to see dialog');
      await click_dialog.getText().should.eventually.equal('Click to see dialog');

      await click_dialog.getAttribute('selected').should.eventually.equal('false');
      await click_dialog.isSelected().should.eventually.be.false;

      await click_dialog.isDisplayed().should.eventually.be.true;

      await click_dialog.getAttribute('class').should.eventually.equal('Text');

      await click_dialog.getAttribute('clickable').should.eventually.equal('false');

      await click_dialog.getAttribute('enabled').should.eventually.equal('true');
      await click_dialog.isEnabled().should.eventually.true;

      await click_dialog.getAttribute('focused').should.eventually.equal('false');
    });
  });
});
