import {fs} from 'appium/support.js';
import {ADB} from 'appium-adb';
import path from 'node:path';
import {exec} from 'teen_process';
import {normalizeVersion} from './version-utils.mjs';

/** @type {Promise<import('appium-adb').ADB> | undefined} */
let sdkAdbPromise;

/**
 * @param {string} dexdumpOutput
 * @returns {string[]}
 */
export function parseKotlinMetadataVersionsFromDexdump(dexdumpOutput) {
  /** @type {Set<string>} */
  const versions = new Set();
  const mvPattern = /mv=\{\s*(\d+)\s+(\d+)\s+(\d+)\s*\}/g;
  let match;
  while ((match = mvPattern.exec(dexdumpOutput)) !== null) {
    const version = normalizeVersion(`${match[1]}.${match[2]}.${match[3]}`);
    if (version) {
      versions.add(version);
    }
  }
  return [...versions];
}

/**
 * @param {string} extractRoot APK extract directory
 * @param {Record<string, Set<string>>} found Module id → version set (mutated)
 */
export async function mergeKotlinMetadataVersionsFromDex(extractRoot, found) {
  if (!found.kotlin) {
    return;
  }
  let dexdump;
  try {
    const adb = await getSdkAdb();
    dexdump = await adb.getSdkBinaryPath('dexdump');
  } catch {
    return;
  }
  let entries;
  try {
    entries = await fs.readdir(extractRoot);
  } catch {
    return;
  }
  for (const name of entries) {
    if (!/^classes\d*\.dex$/i.test(name)) {
      continue;
    }
    try {
      const {stdout} = await exec(dexdump, ['-a', path.join(extractRoot, name)]);
      for (const version of parseKotlinMetadataVersionsFromDexdump(stdout)) {
        found.kotlin.add(version);
      }
    } catch {
      // try next dex file
    }
  }
}

/** @returns {Promise<import('appium-adb').ADB>} */
async function getSdkAdb() {
  if (!sdkAdbPromise) {
    sdkAdbPromise = ADB.createADB({suppressKillServer: true});
  }
  return sdkAdbPromise;
}
