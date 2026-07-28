import {describe, it, before, after} from 'node:test';

import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import type {Browser} from 'webdriverio';

import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

use(chaiAsPromised);

const SKIP_CONTEXT_TESTS = Boolean(process.env.CI);

describe('context', {skip: SKIP_CONTEXT_TESTS, timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:appActivity': 'io.appium.android.apis.view.WebView1',
      }),
    );
  });
  after(async function () {
    await deleteSession();
  });

  it('should get contexts and set them without errors', async function () {
    const viewContexts = await driver.getContexts();

    await expect(driver.getContext()).to.eventually.eql(viewContexts[0]);

    await driver.switchContext(viewContexts[1]);
    await expect(driver.getContext()).to.eventually.eql(viewContexts[1]);

    await driver.switchContext(viewContexts[0]);
    await expect(driver.getContext()).to.eventually.eql(viewContexts[0]);
  });
});
