import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { withMocks } from 'appium-test-support';
import ADB from 'appium-adb';
import B from 'bluebird';
import { EspressoRunner, REQUIRED_PARAMS } from '../../lib/espresso-runner';


chai.should();
chai.use(chaiAsPromised);
const expect = chai.expect;

describe('espresso-runner', function () {
  let adb = new ADB();

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
        expect(() => {
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
  describe('startSession', withMocks({adb}, (mocks) => {
    let opts = getOpts(REQUIRED_PARAMS);
    opts.adb = adb;
    it('should throw an error if running instrumentation process errors', async function () {
      mocks.adb.expects('shell').withExactArgs(["am", "instrument", "-w", "-e", "debug", "false", "io.appium.espressoserver.test/android.support.test.runner.AndroidJUnitRunner"])
        .returns(B.reject("Problem with instrumentation"));

      let espressoRunner = new EspressoRunner(opts);
      await espressoRunner.startSession({}).should.eventually.be.rejectedWith(/Problem with instrumentation/);

      mocks.adb.verify();
    });
  }));
});
