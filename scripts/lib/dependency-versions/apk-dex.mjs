import {fs, zip} from 'appium/support.js';
import path from 'node:path';

/**
 * @param {string} apkPath
 * @param {string} destDir
 */
export async function extractApk(apkPath, destDir) {
  try {
    await zip.extractAllTo(apkPath, destDir);
  } catch (err) {
    throw new Error(`Failed to extract APK.`, {cause: err});
  }
}

/**
 * @param {string} extractRoot
 * @returns {Promise<string>}
 */
export async function readAllDexStrings(extractRoot) {
  /** @type {string[]} */
  const chunks = [];
  let entries;
  try {
    entries = await fs.readdir(extractRoot);
  } catch {
    return '';
  }
  for (const name of entries) {
    if (!/^classes\d*\.dex$/i.test(name)) {
      continue;
    }
    try {
      const buf = await fs.readFile(path.join(extractRoot, name));
      chunks.push(buf.toString('latin1'));
    } catch {
      // ignore
    }
  }
  return chunks.join('\n');
}

/**
 * @param {string} dexStrings
 * @param {string} corpus
 * @returns {boolean}
 */
export function detectProguardLikely(dexStrings, corpus) {
  const androidxHits = (corpus.match(/androidx\//g) ?? []).length;
  const hasR8 = /\bR8\b/.test(corpus) || /proguard/i.test(corpus);
  const fewReadableAndroidx = androidxHits < 3 && dexStrings.length > 100_000;
  const obfuscatedKotlin = (dexStrings.match(/\bL[a-z]{1,2}\/[a-z]{1,2};/g) ?? []).length > 500;
  return (hasR8 && fewReadableAndroidx) || (obfuscatedKotlin && fewReadableAndroidx);
}

/**
 * @param {string} root
 * @returns {Promise<string>}
 */
export async function collectMetaInfStrings(root) {
  const metaDir = path.join(root, 'META-INF');
  /** @type {string[]} */
  const chunks = [];
  /** @param {string} dir */
  async function walkMeta(dir) {
    let entries;
    try {
      entries = await fs.readdir(dir, {withFileTypes: true});
    } catch {
      return;
    }
    for (const entry of entries) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        await walkMeta(full);
      } else if (/\.(properties|version|kotlin_module|txt)$/i.test(entry.name)) {
        try {
          chunks.push(await fs.readFile(full, 'utf8'));
        } catch {
          // ignore binary
        }
      }
    }
  }
  await walkMeta(metaDir);
  return chunks.join('\n');
}
