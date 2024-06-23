import sinon from 'sinon';
import { ADB } from 'appium-adb';
import EspressoDriver from '../../lib/driver';


let sandbox = sinon.createSandbox();

describe('driver', function () {
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
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

  describe('#getProxyAvoidList', function () {
    let driver;
    describe('nativeWebScreenshot', function () {
      let proxyAvoidList;
      let nativeWebScreenshotFilter = (item) => item[0] === 'GET' && item[1].test('/session/xxx/screenshot/');
      beforeEach(function () {
        driver = new EspressoDriver({}, false);
        driver.caps = { appPackage: 'io.appium.package', appActivity: '.MainActivity'};
        driver.opts = { autoLaunch: false, skipUnlock: true, systemPort: 30000 };
        driver.chromedriver = true;
        sandbox.stub(driver, 'initEspressoServer');
        sandbox.stub(driver, 'initAUT');
        sandbox.stub(driver, 'startEspressoSession');
        sandbox.stub(driver, 'getDeviceInfoFromCaps').callsFake(function () {
          return {udid: 1, emPort: 8888};
        });
        sandbox.stub(driver, 'createADB').callsFake(function () {
          return {
            getDevicesWithRetry: () => [{udid: 'emulator-1234'}],
            getPortFromEmulatorString: () => 1234,
            setDeviceId: () => {},
            setEmulatorPort: () => {},
            isAppInstalled: () => true,
            getApiLevel: () => 28,
            forceStop: () => {},
            stopLogcat: () => {},
            setDefaultHiddenApiPolicy: () => {},
          };
        });
      });
      afterEach(function () {
        sandbox.restore();
      });

      it('should proxy screenshot if nativeWebScreenshot is off on chromedriver mode', async function () {
        await driver.createSession(null, null, {
          firstMatch: [{}],
          alwaysMatch: {
            platformName: 'Android',
            'appium:deviceName': 'device',
            'appium:appPackage': driver.caps.appPackage,
            'appium:nativeWebScreenshot': false
          }
        });
        proxyAvoidList = driver.getProxyAvoidList().filter(nativeWebScreenshotFilter);
        proxyAvoidList.should.be.empty;
      });
      it('should not proxy screenshot if nativeWebScreenshot is on on chromedriver mode', async function () {
        await driver.createSession(null, null, {
          firstMatch: [{}],
          alwaysMatch: {
            platformName: 'Android',
            'appium:deviceName': 'device',
            'appium:appPackage': driver.caps.appPackage,
            'appium:nativeWebScreenshot': true
          }
        });
        proxyAvoidList = driver.getProxyAvoidList().filter(nativeWebScreenshotFilter);
        proxyAvoidList.should.not.be.empty;
      });
    });
  });
});
