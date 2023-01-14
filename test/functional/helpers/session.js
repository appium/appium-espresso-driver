import remote from 'webdriverio';
import AsyncLock from 'async-lock';

const SESSION_GUARD = new AsyncLock();
const HOST = process.env.APPIUM_TEST_SERVER_HOST || '127.0.0.1';
const PORT = parseInt(process.env.APPIUM_TEST_SERVER_PORT, 10) || 4567;
const MOCHA_TIMEOUT = (process.env.CI ? 10 : 4) * 60 * 1000;

let driver;

async function initSession (caps) {
  if (driver) {
    await deleteSession();
  }

  return await SESSION_GUARD.acquire(HOST, async () => {
    driver = await remote(caps);
    return driver;
  });
}

async function deleteSession () {
  await SESSION_GUARD.acquire(HOST, async () => {
    try {
      await driver.quit();
    } catch (ign) {}
    driver = null;
  });
}

export { initSession, deleteSession, HOST, PORT, MOCHA_TIMEOUT };
