import log from './logger';
import { server as baseServer, routeConfiguringFunction } from 'appium-base-driver';
import EspressoDriver from './driver';


async function startServer (port, host) {
  const d = new EspressoDriver({port, host});
  const server = baseServer({
    routeConfiguringFunction: routeConfiguringFunction(d),
    port,
    hostname: host,
  });
  log.info(`Android Espresso Driver listening on http://${host}:${port}`);
  return await server;
}

export { startServer };
export default startServer;
