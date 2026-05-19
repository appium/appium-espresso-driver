import path from 'node:path';

// @ts-ignore - __dirname is available at runtime in CommonJS
declare const __dirname: string;

/** Root path of the bundled Espresso server Gradle project. */
export const TEST_SERVER_ROOT = path.resolve(__dirname, '..', '..', '..', 'espresso-server');

export const TEST_APK_PKG = 'io.appium.espressoserver.test';

export const ESPRESSO_SERVER_LAUNCH_TIMEOUT_MS = 45000;

export const TARGET_PACKAGE_CONTAINER = '/data/local/tmp/espresso.apppackage';

/** System port range used to communicate with the Espresso HTTP server on the device. */
export const SYSTEM_PORT_RANGE = [8300, 8399];

/** Port the Espresso server listens on on the device (forwarded from {@link SYSTEM_PORT_RANGE}). */
export const DEVICE_PORT = 6791;
