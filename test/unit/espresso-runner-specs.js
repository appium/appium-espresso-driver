import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { EspressoRunner, REQUIRED_PARAMS } from '../../lib/espresso-runner';
import { ADB } from 'appium-adb';
import { withMocks } from 'appium-test-support';

chai.should();
chai.use(chaiAsPromised);
const expect = chai.expect;

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

  const adb = new ADB();
  describe('installServer', withMocks({adb}, (mocks) => {
    let espresso;
    beforeEach(function () {
      espresso = new EspressoRunner({
        adb, tmpDir: 'tmp', host: 'localhost',
        systemPort: 4724, devicePort: 6790, appPackage: 'io.appium.example',
        forceEspressoRebuild: false
      });
    });
    afterEach(function () {
      mocks.verify();
    });

    it('should uninstall newer server', async function () {
      mocks.adb.expects('getApplicationInstallState').once()
        .returns(adb.APP_INSTALL_STATE.NEWER_VERSION_INSTALLED);
      mocks.adb.expects('uninstallApk').once();
      mocks.adb.expects('installOrUpgrade').once();

      await espresso.installServer();
    });

    it('should not uninstall installed server', async function () {
      mocks.adb.expects('getApplicationInstallState').once()
        .returns(adb.APP_INSTALL_STATE.OLDER_VERSION_INSTALLED);
      mocks.adb.expects('uninstallApk').never();
      mocks.adb.expects('installOrUpgrade').once();

      await espresso.installServer();
    });
  }));
});
