import remote from 'webdriverio';
import { startServer } from '../../server';
import AsyncLock from 'async-lock';

const SESSION_GUARD = new AsyncLock();
const HOST = '127.0.0.1';
const PORT = 4994;
const MOCHA_TIMEOUT = (process.env.CI ? 10 : 4) * 60 * 1000;

let driver, server;

async function initSession (caps) {
  if (driver || server) {
    await deleteSession();
  }

  return await SESSION_GUARD.acquire(HOST, async () => {
    server = await startServer(PORT, HOST);
    driver = await remote(caps);
    return driver;
  });
}

async function deleteSession () {
  await SESSION_GUARD.acquire(HOST, async () => {
    try {
      await driver.quit();
    } catch (ign) {}
    try {
      await server.close();
    } catch (ign) {}
    driver = null;
    server = null;
  });
}

export { initSession, deleteSession, HOST, PORT, MOCHA_TIMEOUT };
