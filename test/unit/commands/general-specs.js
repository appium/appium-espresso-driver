import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import EspressoDriver from '../../../lib/driver';

chai.should();
chai.use(chaiAsPromised);
let sandbox = sinon.createSandbox();

describe('commands', function () {
  describe('general', function () {
    let driver;
    describe('settings', function () {
      beforeEach(function () {
        driver = new EspressoDriver({}, false);
        driver.caps = { appPackage: 'io.appium.package', appActivity: '.MainActivity'};
        driver.opts = { autoLaunch: false, skipUnlock: true };
        sandbox.stub(driver, 'initEspressoServer');
        sandbox.stub(driver, 'initAUT');
        sandbox.stub(driver, 'startEspressoSession');
      });

      it('update settings', async function () {
        await driver.createSession({platformName: 'Android', deviceName: 'device', appPackage: driver.caps.appPackage});
        await driver.updateSettings().should.be.rejectedWith('Method has not yet been implemented');
      });
      it('get settings', async function () {
        await driver.createSession({platformName: 'Android', deviceName: 'device', appPackage: driver.caps.appPackage});
        await driver.getSettings().should.be.rejectedWith('Method has not yet been implemented');
      });
    });
  });
});
