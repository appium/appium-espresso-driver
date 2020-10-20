import wd from 'wd';
import { startServer } from '../../..';


const HOST = '127.0.0.1',
      PORT = 4994;
const MOCHA_TIMEOUT = 60 * 1000 * (process.env.TRAVIS ? 10 : 4);

let driver, server;

async function initSession (caps) {
  if (driver || server) {
    await deleteSession();
  }

  driver = wd.promiseChainRemote(HOST, PORT);
  server = await startServer(PORT, HOST);
  const serverRes = await driver.init(caps);
  if (!caps.udid && !caps.fullReset && serverRes[1].udid) {
    caps.udid = serverRes[1].udid;
  }
  // await driver.setImplicitWaitTimeout(5000);
  return driver;
}

async function deleteSession () {
  try {
    await driver.quit();
  } catch (ign) {}
  try {
    await server.close();
  } catch (ign) {}
  driver = null;
  server = null;
}

export { initSession, deleteSession, HOST, PORT, MOCHA_TIMEOUT };
