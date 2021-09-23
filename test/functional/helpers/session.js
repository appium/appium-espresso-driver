import wd from 'wd';
import { startServer } from '../../..';
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
    driver = wd.promiseChainRemote(HOST, PORT);
    server = await startServer(PORT, HOST);
    const serverRes = await driver.init(caps);
    if (!caps.udid && !caps.fullReset && serverRes[1].udid) {
      caps.udid = serverRes[1].udid;
    }
    // await driver.setImplicitWaitTimeout(5000);
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
