import type {Capabilities} from '@wdio/types';
import {AsyncLock} from 'asyncbox';
import {remote, type Browser} from 'webdriverio';

const SESSION_GUARD = new AsyncLock();
const HOST = process.env.APPIUM_TEST_SERVER_HOST || '127.0.0.1';
const PORT = parseInt(process.env.APPIUM_TEST_SERVER_PORT ?? '', 10) || 4567;
const E2E_TEST_TIMEOUT = (process.env.CI ? 10 : 4) * 60 * 1000;

const COMMON_REMOTE_OPTIONS = {
  hostname: HOST,
  port: PORT,
  // wdio 10 defaults to strict selectors; the tests rely on first-match $()
  strictSelectors: false,
};

// Known emulator startup flakes (mostly API 29); everything else must fail the test
const RETRYABLE_STARTUP_ERROR = /Appium Settings app is not running|DeadSystemException/;

let driver: Browser | null = null;

async function createSession(options: Capabilities.WebdriverIOConfig): Promise<Browser> {
  try {
    return await remote(options);
  } catch (e) {
    const message = (e as Error).message ?? String(e);
    if (!RETRYABLE_STARTUP_ERROR.test(message)) {
      throw e;
    }
    process.stderr.write(`Retrying the session creation once after an emulator startup error: ${message}\n`);
    return await remote(options);
  }
}

async function initSession(caps: Capabilities.RequestedStandaloneCapabilities): Promise<Browser> {
  if (driver) {
    await deleteSession();
  }

  return await SESSION_GUARD.acquire(HOST, async () => {
    const options: Capabilities.WebdriverIOConfig = {
      ...COMMON_REMOTE_OPTIONS,
      capabilities: caps,
    };
    driver = await createSession(options);
    return driver;
  });
}

async function deleteSession(): Promise<void> {
  await SESSION_GUARD.acquire(HOST, async () => {
    try {
      await driver?.deleteSession();
    } catch {}
    driver = null;
  });
}

export {initSession, deleteSession, HOST, PORT, E2E_TEST_TIMEOUT, COMMON_REMOTE_OPTIONS};
