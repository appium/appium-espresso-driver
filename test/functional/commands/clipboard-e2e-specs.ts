import {describe, it, before, after} from 'node:test';
import type {Browser} from 'webdriverio';
import {AssertionError} from 'node:assert';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';
import {APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

// TODO: Enable this in CI after the functional coverage update in the follow-up PR.
const SKIP_CLIPBOARD_TESTS = Boolean(process.env.CI);

describe('clipboard', {skip: SKIP_CLIPBOARD_TESTS, timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get clipboard', async function () {
    await driver.execute('mobile: setClipboard', {
      content: Buffer.from('Hello').toString('base64'),
      contentType: 'plaintext',
    } as any);
    // 'SGVsbG8=' is 'Hello' in base 64 encoding with a new line.
    const text = String(await driver.execute('mobile:getClipboard'));
    try {
      expect(text).to.eql('SGVsbG8=');
    } catch (e) {
      if (e instanceof AssertionError) {
        // API level 23 and 25 emulator has '\n'
        expect(text).to.eql('SGVsbG8=\n');
      } else {
        throw e;
      }
    }
    expect(Buffer.from(text, 'base64').toString()).to.eql('Hello');
  });
});
