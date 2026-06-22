import {describe, it, beforeEach, afterEach} from 'node:test';
import sinon from 'sinon';
import {EspressoDriver} from '../../../lib/driver.js';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';

use(chaiAsPromised);

const sandbox = sinon.createSandbox();

describe('commands', function () {
  afterEach(function () {
    sandbox.restore();
  });

  describe('execute', function () {
    let driver: EspressoDriver;
    beforeEach(function () {
      driver = new EspressoDriver({} as any, false);
    });
    it('should raise error on non-existent mobile command', async function () {
      await expect((driver as any).execute('mobile: fruta', {})).to.be.rejected;
    });
    it('should accept sensorSet on emulator', async function () {
      sandbox.stub(driver, 'isEmulator').returns(true);
      const stub = sandbox.stub(driver, 'sensorSet');
      await (driver as any).execute('mobile: sensorSet', [
        {sensorType: 'acceleration', value: '0:9.77631:0.812349'},
      ]);
      expect(stub.calledOnce).to.equal(true);
      expect(stub.calledWith('acceleration', '0:9.77631:0.812349')).to.be.true;
    });
  });
});
