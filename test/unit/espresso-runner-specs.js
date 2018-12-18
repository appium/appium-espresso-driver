import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { EspressoRunner, REQUIRED_PARAMS, INSTRUMENTATION_TARGET } from '../../lib/espresso-runner';
import { ADB } from 'appium-adb';
import sinon from 'sinon';

chai.should();
chai.use(chaiAsPromised);
const expect = chai.expect;
let sandbox = sinon.createSandbox();

describe('espresso-runner', function () {
  function getOpts (params) {
    let opts = {};
    for (let j = 0; j < params.length; j++) {
      opts[params[j]] = 'value';
    }
    return opts;
  }
  describe('constructor', function () {
    function runConstructorTest (opts, missingParam) {
      it(`should error out if missing '${missingParam}' parameter`, function () {
        expect(function () {
          new EspressoRunner(opts);
        }).to.throw(`Option '${missingParam}' is required!`);
      });
    }
    for (let i = 0; i < REQUIRED_PARAMS.length; i++) {
      let params = REQUIRED_PARAMS.filter((el) => el !== REQUIRED_PARAMS[i]);
      let opts = getOpts(params);
      runConstructorTest(opts, REQUIRED_PARAMS[i]);
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
      shell: () => `${INSTRUMENTATION_TARGET} (target=io.appium.android.apis)\n`,
    };

    afterEach(function () {
      sandbox.restore();
    });

    it('should install newer server', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Object.assign(
          commonStub,
          {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.NEWER_VERSION_INSTALLED,
          }
        );
      });

      const adb = ADB.createADB();
      const espresso = new EspressoRunner({
        adb, tmpDir: 'tmp', host: 'localhost',
        systemPort: 4724, devicePort: 6790, appPackage: 'io.appium.example',
        forceEspressoRebuild: false
      });

      await espresso.installServer();
      espresso.adb.uninstallApk().should.eql(1);
      espresso.adb.install().should.eql(1);
    });

    it('should install older server', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Object.assign(
          commonStub,
          {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.OLDER_VERSION_INSTALLED,
          }
        );
      });

      const adb = ADB.createADB();
      const espresso = new EspressoRunner({
        adb, tmpDir: 'tmp', host: 'localhost',
        systemPort: 4724, devicePort: 6790, appPackage: 'io.appium.example',
        forceEspressoRebuild: false
      });

      await espresso.installServer();
      espresso.adb.uninstallApk().should.eql(1);
      espresso.adb.install().should.eql(1);
    });

    it('should install from no server', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Object.assign(
          commonStub,
          {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.NOT_INSTALLED
          }
        );
      });

      const adb = ADB.createADB();
      const espresso = new EspressoRunner({
        adb, tmpDir: 'tmp', host: 'localhost',
        systemPort: 4724, devicePort: 6790, appPackage: 'io.appium.example',
        forceEspressoRebuild: false
      });

      await espresso.installServer();
      espresso.adb.uninstallApk().should.eql(0);
      espresso.adb.install().should.eql(1);
    });
  });
});
