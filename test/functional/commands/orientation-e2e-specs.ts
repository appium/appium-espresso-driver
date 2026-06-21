import {describe, it, before, after} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import type {Browser} from 'webdriverio';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';
import {APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

describe('orientation', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get orientation', async function () {
    await expect(driver.getOrientation()).to.eventually.eql('PORTRAIT');

    await driver.setOrientation('landscape');
    await expect(driver.getOrientation()).to.eventually.eql('LANDSCAPE');

    await driver.setOrientation('PORTRAIT');
    await expect(driver.getOrientation()).to.eventually.eql('PORTRAIT');
  });
});
