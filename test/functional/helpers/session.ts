import {remote, type Browser} from 'webdriverio';
import AsyncLock from 'async-lock';
import type {Capabilities} from '@wdio/types';

const SESSION_GUARD = new AsyncLock();
const HOST = process.env.APPIUM_TEST_SERVER_HOST || '127.0.0.1';
const PORT = parseInt(process.env.APPIUM_TEST_SERVER_PORT ?? '', 10) || 4567;
const E2E_TEST_TIMEOUT = (process.env.CI ? 10 : 4) * 60 * 1000;

const COMMON_REMOTE_OPTIONS = {
  hostname: HOST,
  port: PORT,
};

let driver: Browser | null = null;

async function initSession(caps: Capabilities.RequestedStandaloneCapabilities): Promise<Browser> {
  if (driver) {
    await deleteSession();
  }

  return await SESSION_GUARD.acquire(HOST, async () => {
    const options: Capabilities.WebdriverIOConfig = {
      ...COMMON_REMOTE_OPTIONS,
      capabilities: caps,
    };
    driver = await remote(options);
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
