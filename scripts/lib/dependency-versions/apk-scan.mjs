import {fs, tempDir} from 'appium/support.js';
import {createEmptyVersionSets} from './tracked-modules.mjs';
import {collectVersionsFromCorpus, versionSetsToSortedRecords} from './version-utils.mjs';
import {extractApk, readAllDexStrings, detectProguardLikely, collectMetaInfStrings} from './apk-dex.mjs';
import {mergeMetaInfEmbeddedVersions} from './apk-meta-inf.mjs';
import {mergeKotlinMetadataVersionsFromDex} from './apk-kotlin-metadata.mjs';

/**
 * @param {string} apkPath
 * @returns {Promise<{versions: Record<string, string[]>, proguardLikely: boolean, sources: string[]}>}
 */
export async function collectAppVersionsFromApk(apkPath) {
  const extractDir = await tempDir.openDir();
  try {
    await extractApk(apkPath, extractDir);
    const dexStrings = await readAllDexStrings(extractDir);
    const metaStrings = await collectMetaInfStrings(extractDir);
    const found = createEmptyVersionSets();
    const corpus = `${dexStrings}\n${metaStrings}`;

    collectVersionsFromCorpus(corpus, found);
    await mergeMetaInfEmbeddedVersions(extractDir, found);
    await mergeKotlinMetadataVersionsFromDex(extractDir, found);

    const proguardLikely = detectProguardLikely(dexStrings, corpus);

    return {
      versions: versionSetsToSortedRecords(found),
      proguardLikely,
      sources: ['APK DEX/META-INF scan (incl. embedded *.version metadata)'],
    };
  } finally {
    await fs.rimraf(extractDir);
  }
}
