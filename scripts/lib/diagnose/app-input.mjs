import {fs} from 'appium/support.js';
import path from 'node:path';
import {
  collectAppVersionsFromApk,
  collectAppVersionsFromProject,
  loadEspressoServerVersions,
} from '../dependency-versions/index.mjs';
import {detectApkInternetPermission} from './apk-manifest.mjs';
import {parseGradleProperties} from './gradle-utils.mjs';
import {findManifestTexts} from './project-input.mjs';

/**
 * @param {string} espressoServerRoot
 * @param {string} driverRoot
 * @returns {Promise<import('./types.mjs').EspressoServerDefaults>}
 */
export async function loadEspressoServerDefaults(espressoServerRoot, driverRoot) {
  const [versions, gradlePropsText, pkgText] = await Promise.all([
    loadEspressoServerVersions(espressoServerRoot),
    fs.readFile(path.join(espressoServerRoot, 'gradle.properties'), 'utf8'),
    fs.readFile(path.join(driverRoot, 'package.json'), 'utf8'),
  ]);
  const gradleProps = parseGradleProperties(gradlePropsText);
  const driverVersion = JSON.parse(pkgText).version;
  return {
    driverVersion,
    compileSdk: gradleProps.appiumCompileSdk ?? null,
    minSdk: gradleProps.appiumMinSdk ?? null,
    versions,
  };
}

/**
 * @param {string} projectRoot
 * @returns {Promise<import('./types.mjs').AppInput>}
 */
export async function collectAppInputFromProject(projectRoot) {
  const {versions, minifyEnabled, sources} = await collectAppVersionsFromProject(projectRoot);
  const gradleFiles = sources.map((rel) => path.join(projectRoot, rel));
  const gradleCorpus = (
    await Promise.all(gradleFiles.map((f) => fs.readFile(f, 'utf8').catch(() => '')))
  ).join('\n');
  const manifestTexts = await findManifestTexts(projectRoot);

  return {
    kind: 'project',
    path: projectRoot,
    versions,
    proguardLikely: false,
    minifyEnabled,
    sources,
    gradleCorpus,
    manifestPaths: manifestTexts,
    apkHasInternetPermission: null,
  };
}

/**
 * @param {string} apkPath
 * @returns {Promise<import('./types.mjs').AppInput>}
 */
export async function collectAppInputFromApk(apkPath) {
  const {versions, proguardLikely, sources} = await collectAppVersionsFromApk(apkPath);
  const apkHasInternetPermission = await detectApkInternetPermission(apkPath);
  return {
    kind: 'apk',
    path: apkPath,
    versions,
    proguardLikely,
    minifyEnabled: null,
    sources,
    apkHasInternetPermission,
  };
}
