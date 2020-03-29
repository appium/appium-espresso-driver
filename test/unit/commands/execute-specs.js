import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import sinon from 'sinon';
import EspressoDriver from '../../../lib/driver';

chai.should();
chai.use(chaiAsPromised);
let sandbox = sinon.createSandbox();

describe('commands', function () {
  describe('execute', function () {
    let driver;
    beforeEach(function () {
      driver = new EspressoDriver({}, false);
    });
    afterEach(function () {
      sandbox.reset();
    });
    it('should raise error on non-existent mobile command', async function () {
      await driver.executeMobile('fruta', {}).should.eventually.be.rejectedWith('Unknown mobile command "fruta"');
    });
    it('should accept sensorSet on emulator', async function () {
      sandbox.stub(driver, 'isEmulator').returns(true);
      let stub = sandbox.stub(driver, 'sensorSet');
      await driver.executeMobile('sensorSet', { sensorType: 'acceleration', value: '0:9.77631:0.812349' });
      stub.calledOnce.should.equal(true);
      stub.calledWithExactly({ sensorType: 'acceleration', value: '0:9.77631:0.812349' });
    });
  });
});
