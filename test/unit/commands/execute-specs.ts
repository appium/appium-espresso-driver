import assert from 'node:assert/strict';
import {describe, it, beforeEach, afterEach} from 'node:test';

import sinon from 'sinon';

import {EspressoDriver} from '../../../lib/driver.js';

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
      await assert.rejects((driver as any).execute('mobile: fruta', {}));
    });
    it('should accept sensorSet on emulator', async function () {
      sandbox.stub(driver, 'isEmulator').returns(true);
      const stub = sandbox.stub(driver, 'sensorSet');
      await (driver as any).execute('mobile: sensorSet', [{sensorType: 'acceleration', value: '0:9.77631:0.812349'}]);
      assert.strictEqual(stub.calledOnce, true);
      assert.strictEqual(stub.calledWith('acceleration', '0:9.77631:0.812349'), true);
    });
  });

  describe('startActivity', function () {
    let driver: EspressoDriver;
    let proxyCommandStub: sinon.SinonStub;
    let waitForActivityStub: sinon.SinonStub;

    beforeEach(function () {
      driver = new EspressoDriver({} as any, false);
      driver.caps = {appPackage: 'io.appium.android.apis'} as any;
      proxyCommandStub = sandbox.stub().resolves();
      driver.espresso = {jwproxy: {command: proxyCommandStub}} as any;
      waitForActivityStub = sandbox.stub().resolves();
      driver.adb = {waitForActivity: waitForActivityStub} as any;
    });

    it('should qualify relative activity name and wait for activity', async function () {
      await driver.startActivity('io.appium.android.apis', '.accessibility.AccessibilityNodeProviderActivity');
      assert.strictEqual(proxyCommandStub.calledOnce, true);
      assert.deepStrictEqual(proxyCommandStub.firstCall.args, [
        '/appium/device/start_activity',
        'POST',
        {
          appPackage: 'io.appium.android.apis',
          appActivity: 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
        },
      ]);
      assert.strictEqual(waitForActivityStub.calledOnce, true);
      assert.deepStrictEqual(waitForActivityStub.firstCall.args, [
        'io.appium.android.apis',
        'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
      ]);
    });

    it('should preserve fully-qualified activity name and wait for activity', async function () {
      await driver.startActivity(
        'io.appium.android.apis',
        'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
      );
      assert.strictEqual(proxyCommandStub.calledOnce, true);
      assert.deepStrictEqual(proxyCommandStub.firstCall.args, [
        '/appium/device/start_activity',
        'POST',
        {
          appPackage: 'io.appium.android.apis',
          appActivity: 'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
        },
      ]);
      assert.strictEqual(waitForActivityStub.calledOnce, true);
      assert.deepStrictEqual(waitForActivityStub.firstCall.args, [
        'io.appium.android.apis',
        'io.appium.android.apis.accessibility.AccessibilityNodeProviderActivity',
      ]);
    });
  });
});
