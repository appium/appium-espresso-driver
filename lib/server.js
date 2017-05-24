import log from './logger';
import { server as baseServer, routeConfiguringFunction } from 'appium-base-driver';
import EspressoDriver from './driver';


async function startServer (port, host) {
  let d = new EspressoDriver({port, host});
  let router = routeConfiguringFunction(d);
  let server = baseServer(router, port, host);
  log.info(`Android Espresso Driver listening on http://${host}:${port}`);
  return await server;
}

export default startServer;
