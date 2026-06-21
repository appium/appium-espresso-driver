import {describe, it, before, after} from 'node:test';
import type {Browser} from 'webdriverio';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession} from '../helpers/session.js';
import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

describe('context', function () {

  let driver: Browser;

  before(async function (t) {
    // We are getting rate limited by the API when running on CI.
    if (process.env.CI) {
      (t as any).skip();
    }

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
