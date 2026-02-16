import {EspressoRunner} from '../../lib/espresso-runner';
import {ADB} from 'appium-adb';
import sinon from 'sinon';
import {log} from '../../lib/logger';
import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';

const {expect} = chai;
chai.use(chaiAsPromised);

const REQUIRED_PARAMS = [
  'adb',
  'tmpDir',
  'host',
  'systemPort',
  'devicePort',
  'appPackage',
  'forceEspressoRebuild',
] as const;

const sandbox = sinon.createSandbox();

describe('espresso-runner', function () {
  this.afterEach(function () {
    sandbox.restore();
  });

  function getOpts(params: string[]) {
    const opts: any = {};
    for (const param of params) {
      opts[param] = 'value';
    }
    return opts;
  }
  describe('constructor', function () {
    function runConstructorTest(opts: any, missingParam: string) {
      it(`should error out if missing '${missingParam}' parameter`, function () {
        expect(function () {
          new EspressoRunner(log, opts);
        }).to.throw(`Option '${missingParam}' is required!`);
      });
    }
    for (const requiredParam of REQUIRED_PARAMS) {
      const params = REQUIRED_PARAMS.filter((el) => el !== requiredParam);
      const opts = getOpts(params);
      runConstructorTest(opts, requiredParam);
    }
  });

  describe('installServer', function () {
    const adbCmd = new ADB();
    let uninstallCount = -1;
    let installCount = -1;
    const commonStub = {
      APP_INSTALL_STATE: {
        NEWER_VERSION_INSTALLED: adbCmd.APP_INSTALL_STATE.NEWER_VERSION_INSTALLED,
        OLDER_VERSION_INSTALLED: adbCmd.APP_INSTALL_STATE.OLDER_VERSION_INSTALLED,
        NOT_INSTALLED: adbCmd.APP_INSTALL_STATE.NOT_INSTALLED,
      },
      uninstallApk: () => {
        uninstallCount += 1;
        return uninstallCount;
      },
      install: () => {
        installCount += 1;
        return installCount;
      },
    };

    it('should install newer server', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Promise.resolve(
          Object.assign(commonStub, {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.NEWER_VERSION_INSTALLED,
          }) as any,
        );
      });

      const adb = await ADB.createADB();
      const espresso = new EspressoRunner(log, {
        adb,
        tmpDir: 'tmp',
        host: 'localhost',
        systemPort: 4724,
        devicePort: 6790,
        appPackage: 'io.appium.example',
        forceEspressoRebuild: false,
      });

      await espresso.installServer();
      expect((espresso.adb as any).uninstallApk()).to.eql(1);
      expect((espresso.adb as any).install()).to.eql(1);
    });

    it('should install older server', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Promise.resolve(
          Object.assign(commonStub, {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.OLDER_VERSION_INSTALLED,
          }) as any,
        );
      });

      const adb = await ADB.createADB();
      const espresso = new EspressoRunner(log, {
        adb,
        tmpDir: 'tmp',
        host: 'localhost',
        systemPort: 4724,
        devicePort: 6790,
        appPackage: 'io.appium.example',
        forceEspressoRebuild: false,
      });

      await espresso.installServer();
      expect((espresso.adb as any).uninstallApk()).to.eql(1);
      expect((espresso.adb as any).install()).to.eql(1);
    });

    it('should install from no server', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Promise.resolve(
          Object.assign(commonStub, {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.NOT_INSTALLED,
          }) as any,
        );
      });

      const adb = await ADB.createADB();
      const espresso = new EspressoRunner(log, {
        adb,
        tmpDir: 'tmp',
        host: 'localhost',
        systemPort: 4724,
        devicePort: 6790,
        appPackage: 'io.appium.example',
        forceEspressoRebuild: false,
      });

      await espresso.installServer();
      expect(espresso.adb.uninstallApk('io.appium.espressoserver.test')).to.eql(0);
      expect(espresso.adb.install('path/to/apk')).to.eql(1);
    });

    it('should raise an error when it fails to install an apk', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Promise.resolve(
          Object.assign(commonStub, {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.NOT_INSTALLED,
            install: () => {
              throw new Error('error happened');
            },
          }) as any,
        );
      });

      const adb = await ADB.createADB();
      const espresso = new EspressoRunner(log, {
        adb,
        tmpDir: 'tmp',
        host: 'localhost',
        systemPort: 4724,
        devicePort: 6790,
        appPackage: 'io.appium.example',
        forceEspressoRebuild: false,
      });

      await expect(espresso.installServer()).to.be.rejectedWith(/error happened/i);
      expect((espresso.adb as any).uninstallApk()).to.eql(0);
    });
  });
});
