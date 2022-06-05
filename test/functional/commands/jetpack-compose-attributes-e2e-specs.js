import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { remote } from 'webdriverio';
import { MOCHA_TIMEOUT, HOST, PORT } from '../helpers/session';
import { COMPOSE_CAPS } from '../desired';
import { startServer } from '../../..';


chai.should();
chai.use(chaiAsPromised);

const COMMON_REMOTE_OPTIONS = {
  hostname: HOST,
  port: PORT,
};

describe('compose node attributes', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let server;

  before(async function () {
    // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
    if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 23) {
      return this.skip();
    }

    server = await startServer(PORT, HOST);
  });
  after(async function () {
    try {
      await server.close();
    } catch (ign) {}
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

    it(`should get the 'content-desc' of a View`, async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: COMPOSE_CAPS,
      });

      const el = await driver.$("//*[@text='Clickable Component']");
      await el.click();

      await driver.updateSettings({ driver: 'compose' });

      const taggedElement = await driver.findElement('tag name', 'lol');
      await taggedElement.getAttribute('view-tag').should.eventually.equal('lol');
    });

    it(`should get the 'text' of a View`, async function () {
      driver = await remote({
        ...COMMON_REMOTE_OPTIONS,
        capabilities: COMPOSE_CAPS,
      });

      const el = await driver.$("//*[@text='Clickable Component']");
      await el.click();

      await driver.updateSettings({ driver: 'compose' });

      const click_dialog = await driver.$('Click to see dialog');
      await click_dialog.getAttribute('text').should.eventually.equal('Click to see dialog');

      const selected = await el.getAttribute('selected');
      await el.isSelected().should.eventually.equal(selected);
      // el.getAttribute('class');
      // el.getAttribute('clickable');
      // el.getAttribute('content-desc');
      // el.getAttribute('enabled');
      // el.getAttribute('focused');
      // el.getAttribute('text');
    });
  });
});
