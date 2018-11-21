import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import { ADB } from 'appium-adb';
import EspressoDriver from '../../lib/driver';
import EspressoRunner from '../../lib/espresso-runner';

chai.should();
chai.use(chaiAsPromised);
let sandbox = sinon.createSandbox();

describe('driver', function () {
  describe('startEspressoSession', function () {
    let driver;
    beforeEach(function () {
      driver = new EspressoDriver({}, false);
      driver.caps = { appPackage: 'io.appium.package', appActivity: '.MainActivity'};
      driver.opts = { autoLaunch: false, skipUnlock: true };
      sandbox.stub(driver, 'initEspressoServer');
      sandbox.stub(driver, 'addDeviceInfoToCaps');
      driver.espresso = new EspressoRunner({
        adb: ADB.createADB(), tmpDir: 'tmp', systemPort: 4724, host: 'localhost', devicePort: 6790, appPackage: driver.caps.appPackage, forceEspressoRebuild: false
      });
      sandbox.stub(driver.espresso, 'startSession');
    });
    afterEach(function () {
      sandbox.restore();
    });
    it('should call setHiddenApiPolicy', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        let calledCount = 0;
        return {
          getDevicesWithRetry: () => [{udid: 'emulator-1234'}],
          getPortFromEmulatorString: () => 1234,
          setDeviceId: () => {},
          setEmulatorPort: () => {},
          networkSpeed: () => {},
          getApiLevel: () => 28,
          setHiddenApiPolicy: () => {
            calledCount += 1;
            return calledCount;
          },
          waitForDevice: () => {},
          processExists: () => true, // skip launching avd
          startLogcat: () => {},
          forwardPort: () => {},
          isAnimationOn: () => false,
        };
      });
      await driver.startEspressoSession();
      driver.adb.setHiddenApiPolicy().should.eql(2);
    });
    it('should not call setHiddenApiPolicy', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        let calledCount = 0;
        return {
          getDevicesWithRetry: () => [{udid: 'emulator-1234'}],
          getPortFromEmulatorString: () => 1234,
          setDeviceId: () => {},
          setEmulatorPort: () => {},
          networkSpeed: () => {},
          getApiLevel: () => 27,
          setHiddenApiPolicy: () => {
            calledCount += 1;
            return calledCount;
          },
          waitForDevice: () => {},
          processExists: () => true, // skip launching avd
          startLogcat: () => {},
          forwardPort: () => {},
          isAnimationOn: () => false,
        };
      });
      await driver.startEspressoSession();
      driver.adb.setHiddenApiPolicy().should.eql(1);
    });
  });

  describe('deleteSession', function () {
    let driver;
    beforeEach(function () {
      driver = new EspressoDriver({}, false);
      driver.adb = new ADB();
      driver.caps = {};
      sandbox.stub(driver.adb, 'stopLogcat');
    });
    afterEach(function () {
      sandbox.restore();
    });
    it('should call setDefaultHiddenApiPolicy', async function () {
      sandbox.stub(driver.adb, 'getApiLevel').returns(28);
      sandbox.stub(driver.adb, 'setDefaultHiddenApiPolicy');
      await driver.deleteSession();
      driver.adb.setDefaultHiddenApiPolicy.calledOnce.should.be.true;
    });
    it('should not call setDefaultHiddenApiPolicy', async function () {
      sandbox.stub(driver.adb, 'getApiLevel').returns(27);
      sandbox.stub(driver.adb, 'setDefaultHiddenApiPolicy');
      await driver.deleteSession();
      driver.adb.setDefaultHiddenApiPolicy.calledOnce.should.be.false;
    });
  });
});
