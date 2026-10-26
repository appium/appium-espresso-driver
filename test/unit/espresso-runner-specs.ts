import assert from 'node:assert/strict';
import {describe, it, afterEach} from 'node:test';

import {ADB} from 'appium-adb';
import {fs} from 'appium/support.js';
import sinon from 'sinon';

import {EspressoRunner, ServerBuilder} from '../../lib/commands/server/index.js';
import {log} from '../../lib/logger.js';

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
  afterEach(function () {
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
        assert.throws(
          function () {
            new EspressoRunner(log, opts);
          },
          new Error(`Option '${missingParam}' is required!`),
        );
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
      assert.strictEqual((espresso.adb as any).uninstallApk(), 1);
      assert.strictEqual((espresso.adb as any).install(), 1);
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
      assert.strictEqual((espresso.adb as any).uninstallApk(), 1);
      assert.strictEqual((espresso.adb as any).install(), 1);
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
      assert.strictEqual(espresso.adb.uninstallApk('io.appium.espressoserver.test'), 0);
      assert.strictEqual(espresso.adb.install('path/to/apk'), 1);
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

      await assert.rejects(espresso.installServer(), /error happened/i);
      assert.strictEqual((espresso.adb as any).uninstallApk(), 0);
    });
  });

  describe('buildNewModServer', function () {
    it('should continue building when cleaning up the previous build folder fails with EBUSY', async function () {
      const ebusyError = Object.assign(
        new Error("EBUSY: resource busy or locked, rmdir 'espresso-server-emulator-5554'"),
        {
          code: 'EBUSY',
        },
      );
      sandbox.stub(fs, 'rimraf').rejects(ebusyError);
      sandbox.stub(fs, 'mkdirp').resolves();
      sandbox.stub(fs, 'walkDir').resolves();
      const copyFileStub = sandbox.stub(fs, 'copyFile').resolves();
      const buildStub = sandbox.stub(ServerBuilder.prototype, 'build').resolves();

      const espresso = new EspressoRunner(log, {
        adb: {curDeviceId: 'emulator-5554'} as any,
        tmpDir: 'tmp',
        host: 'localhost',
        systemPort: 4724,
        devicePort: 6790,
        appPackage: 'io.appium.example',
        forceEspressoRebuild: true,
      });

      await espresso.buildNewModServer();
      assert.strictEqual(buildStub.calledOnce, true);
      assert.strictEqual(copyFileStub.calledOnce, true);
    });
  });
});
