import {fs} from 'appium/support.js';
import path from 'node:path';
import _fs from 'node:fs';
import {fileURLToPath} from 'node:url';

let PACKAGE_INFO: {manifestPath: string; manifestPayload: Record<string, any>} | null = null;
const MODULE_NAME = 'appium-espresso-driver';
const FILENAME = fileURLToPath(import.meta.url);

/**
 * Fetches the module info from package.json
 *
 * @returns {Promise<Record<string, any>>} The full path to module's package.json and its payload
 * @throws {Error} If package.json cannot be found
 */
export async function getPackageInfo(): Promise<{
  manifestPath: string;
  manifestPayload: Record<string, any>;
}> {
  if (PACKAGE_INFO) {
    return PACKAGE_INFO;
  }

  let currentDir = path.dirname(FILENAME);
  let isAtFsRoot = false;
  while (!isAtFsRoot) {
    const manifestPath = path.join(currentDir, 'package.json');
    try {
      if (await fs.exists(manifestPath)) {
        const manifestPayload = JSON.parse(await fs.readFile(manifestPath, 'utf8'));
        if (manifestPayload.name === MODULE_NAME) {
          PACKAGE_INFO = {
            manifestPath,
            manifestPayload,
          };
          return PACKAGE_INFO;
        }
      }
    } catch {}
    currentDir = path.dirname(currentDir);
    isAtFsRoot = currentDir.length <= path.dirname(currentDir).length;
  }
  throw new Error(`Cannot find the root folder of the ${MODULE_NAME} Node.js module`);
}

/**
 * Fetches the module info from package.json synchronously
 *
 * @returns {Record<string, any>} The full path to module's package.json and its payload
 * @throws {Error} If package.json cannot be found
 */
export function getPackageInfoSync(): {manifestPath: string; manifestPayload: Record<string, any>} {
  if (PACKAGE_INFO) {
    return PACKAGE_INFO;
  }

  let currentDir = path.dirname(FILENAME);
  let isAtFsRoot = false;
  while (!isAtFsRoot) {
    const manifestPath = path.join(currentDir, 'package.json');
    try {
      if (_fs.existsSync(manifestPath)) {
        const manifestPayload = JSON.parse(_fs.readFileSync(manifestPath, 'utf8'));
        if (manifestPayload.name === MODULE_NAME) {
          PACKAGE_INFO = {
            manifestPath,
            manifestPayload,
          };
          return PACKAGE_INFO;
        }
      }
    } catch {}
    currentDir = path.dirname(currentDir);
    isAtFsRoot = currentDir.length <= path.dirname(currentDir).length;
  }
  throw new Error(`Cannot find the root folder of the ${MODULE_NAME} Node.js module`);
}
