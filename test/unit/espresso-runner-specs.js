import { EspressoRunner, REQUIRED_PARAMS } from '../../lib/espresso-runner';
import { ADB } from 'appium-adb';
import sinon from 'sinon';
import log from '../../lib/logger';

let sandbox = sinon.createSandbox();

describe('espresso-runner', function () {
  let chai;
  let expect;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
    expect = chai.expect;
  });

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
          new EspressoRunner(log, opts);
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
      }
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
      const espresso = new EspressoRunner(log, {
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
      const espresso = new EspressoRunner(log, {
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
      const espresso = new EspressoRunner(log, {
        adb, tmpDir: 'tmp', host: 'localhost',
        systemPort: 4724, devicePort: 6790, appPackage: 'io.appium.example',
        forceEspressoRebuild: false
      });

      await espresso.installServer();
      espresso.adb.uninstallApk().should.eql(0);
      espresso.adb.install().should.eql(1);
    });

    it('should raise an error when it fails to install an apk', async function () {
      sandbox.stub(ADB, 'createADB').callsFake(function () {
        uninstallCount = -1;
        installCount = -1;
        return Object.assign(
          commonStub,
          {
            getApplicationInstallState: () => adbCmd.APP_INSTALL_STATE.NOT_INSTALLED,
            install: () => {
              throw new Error('error happened');
            }
          }
        );
      });

      const adb = ADB.createADB();
      const espresso = new EspressoRunner(log, {
        adb, tmpDir: 'tmp', host: 'localhost',
        systemPort: 4724, devicePort: 6790, appPackage: 'io.appium.example',
        forceEspressoRebuild: false
      });

      await espresso.installServer().should.eventually.to.be.rejectedWith(/error happened/i);
      espresso.adb.uninstallApk().should.eql(0);
    });
  });
});
