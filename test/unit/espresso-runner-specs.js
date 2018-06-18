import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { EspressoRunner, REQUIRED_PARAMS } from '../../lib/espresso-runner';


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
});
