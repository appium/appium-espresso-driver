import sinon from 'sinon';
import {ADB} from 'appium-adb';
import {EspressoDriver} from '../../lib/driver';
import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';

const {expect} = chai;
chai.use(chaiAsPromised);

const sandbox = sinon.createSandbox();

describe('driver', function () {
  this.afterEach(function () {
    sandbox.restore();
  });

  describe('deleteSession', function () {
    let driver: EspressoDriver;
    beforeEach(function () {
      driver = new EspressoDriver({} as any, false);
      driver.adb = new ADB();
      driver.caps = {} as any;
      sandbox.stub(driver.adb, 'stopLogcat');
    });
    it('should call setDefaultHiddenApiPolicy', async function () {
      sandbox.stub(driver.adb, 'getApiLevel').resolves(28);
      const setDefaultHiddenApiPolicyStub = sandbox.stub(driver.adb, 'setDefaultHiddenApiPolicy');
      await driver.deleteSession();
      expect(setDefaultHiddenApiPolicyStub.calledOnce).to.be.true;
    });
    it('should not call setDefaultHiddenApiPolicy', async function () {
      sandbox.stub(driver.adb, 'getApiLevel').resolves(27);
      const setDefaultHiddenApiPolicyStub = sandbox.stub(driver.adb, 'setDefaultHiddenApiPolicy');
      await driver.deleteSession();
      expect(setDefaultHiddenApiPolicyStub.calledOnce).to.be.false;
    });
  });

  describe('#getProxyAvoidList', function () {
    let driver: EspressoDriver;
    describe('nativeWebScreenshot', function () {
      let proxyAvoidList: Array<[string, RegExp]>;
      const nativeWebScreenshotFilter = (item: [string, RegExp]) =>
        item[0] === 'GET' && item[1].test('/session/xxx/screenshot/');
      beforeEach(function () {
        driver = new EspressoDriver({} as any, false);
        driver.caps = {appPackage: 'io.appium.package', appActivity: '.MainActivity'} as any;
        driver.opts = {autoLaunch: false, skipUnlock: true, systemPort: 30000} as any;
        driver.chromedriver = true as any;
        sandbox.stub(driver, 'initEspressoServer');
        sandbox.stub(driver, 'initAUT');
        sandbox.stub(driver, 'startEspressoSession');
        sandbox.stub(driver, 'getDeviceInfoFromCaps').callsFake(function () {
          return Promise.resolve({udid: '1', emPort: 8888});
        });
        sandbox.stub(driver, 'createADB').callsFake(function () {
          return Promise.resolve({
            getDevicesWithRetry: () => [{udid: 'emulator-1234'}],
            getPortFromEmulatorString: () => 1234,
            setDeviceId: () => {},
            setEmulatorPort: () => {},
            isAppInstalled: () => true,
            getApiLevel: () => Promise.resolve(28),
            forceStop: () => {},
            stopLogcat: () => {},
            setDefaultHiddenApiPolicy: () => {},
          } as any);
        });
      });

      it('should proxy screenshot if nativeWebScreenshot is off on chromedriver mode', async function () {
        await driver.createSession({
          firstMatch: [{}],
          alwaysMatch: {
            platformName: 'Android',
            'appium:deviceName': 'device',
            'appium:appPackage': driver.caps.appPackage,
            'appium:nativeWebScreenshot': false,
          },
        } as any);
        proxyAvoidList = driver
          .getProxyAvoidList(driver.sessionId)
          .filter(nativeWebScreenshotFilter);
        expect(proxyAvoidList).to.be.empty;
      });
      it('should not proxy screenshot if nativeWebScreenshot is on on chromedriver mode', async function () {
        await driver.createSession({
          firstMatch: [{}],
          alwaysMatch: {
            platformName: 'Android',
            'appium:deviceName': 'device',
            'appium:appPackage': driver.caps.appPackage,
            'appium:nativeWebScreenshot': true,
          },
        } as any);
        proxyAvoidList = driver
          .getProxyAvoidList(driver.sessionId)
          .filter(nativeWebScreenshotFilter);
        expect(proxyAvoidList).to.not.be.empty;
      });
    });
  });
});
