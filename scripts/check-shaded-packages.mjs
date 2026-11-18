#!/usr/bin/env node
/**
 * Fails if the Espresso server test APK defines classes outside of the packages that are allowed to
 * keep their names (see `RelocateClassesTask` usage in espresso-server/app/build.gradle.kts).
 */
import path from 'node:path';

import {logger} from 'appium/support.js';
import {Command} from 'commander';
import {exec} from 'teen_process';

const LOG = logger.getLogger('CheckShadedPackages');

const ALWAYS_ALLOWED = [
  'io.appium',
  'androidx.test.runner',
  'androidx.test.core.app',
  'androidx.test.espresso.idling',
  // D8 desugaring stubs and the platform
  'android',
  'dalvik',
  'j$',
  'com.android.tools.r8',
];
// Exactly these packages, without subpackages: D8 synthetic classes live in the first two,
// `IdlingResource` (implemented by the AUT) in the last one
const EXACT_PACKAGES = ['androidx', 'com', 'androidx.test.espresso'];
// Shared with the AUT together with their reference closure
const COMPOSE_ALLOWED = ['androidx', 'kotlin', 'kotlinx', '_COROUTINE', 'org.jetbrains', 'org.intellij'];

/**
 * @returns {string}
 */
function getApkAnalyzerPath() {
  const sdkRoot = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
  return process.env.APKANALYZER || path.join(sdkRoot ?? '', 'cmdline-tools', 'latest', 'bin', 'apkanalyzer');
}

/**
 * @param {string} apk
 * @returns {Promise<Map<string, number>>} Package name to the number of classes in it and its subpackages
 */
async function listDefinedPackages(apk) {
  const {stdout} = await exec(getApkAnalyzerPath(), ['dex', 'packages', '--defined-only', apk], {
    maxStdoutBufferSize: 1 << 28,
  });
  return new Map(
    stdout
      .split('\n')
      .map((line) => line.split('\t'))
      .filter(([kind, , , name]) => kind?.startsWith('P') && name && name !== '<TOTAL>')
      .map(([, count, , name]) => [name, Number(count)]),
  );
}

/**
 * @param {Map<string, number>} packages
 * @param {readonly string[]} allowed
 * @returns {{name: string, own: number}[]} Packages with classes defined right in them that are not allowed
 */
function findViolations(packages, allowed) {
  /** @param {string} pkg */
  const isAllowed = (pkg) => EXACT_PACKAGES.includes(pkg) || allowed.some((a) => pkg === a || pkg.startsWith(`${a}.`));
  // The listing is a tree where every package carries the counts of its subpackages too
  return [...packages]
    .map(([name, count]) => {
      const children = [...packages].filter(
        ([n]) => n.startsWith(`${name}.`) && !n.slice(name.length + 1).includes('.'),
      );
      return {name, own: count - children.reduce((sum, [, c]) => sum + c, 0)};
    })
    .filter(({name, own}) => own > 0 && !isAllowed(name));
}

/**
 * @param {string} apk
 * @param {{compose?: boolean}} options
 * @returns {Promise<void>}
 */
async function checkShadedPackages(apk, options) {
  const allowed = [...ALWAYS_ALLOWED, ...(options.compose ? COMPOSE_ALLOWED : [])];
  const violations = findViolations(await listDefinedPackages(apk), allowed);
  if (violations.length > 0) {
    LOG.error(
      `The APK defines classes outside of the allowed packages (${allowed.join(', ')}):\n` +
        violations.map(({name, own}) => `  ${name} (${own})`).join('\n'),
    );
    process.exitCode = 1;
    return;
  }
  LOG.info('OK: all the packages defined by the APK are allowed');
}

async function main() {
  const program = new Command();
  program
    .name('check-shaded-packages')
    .description('Check that the Espresso server APK defines only the packages that are allowed to keep their names')
    .argument('<apk>', 'Path to the Espresso server test APK')
    .option('--compose', 'The APK is built with Compose support, so Compose and its dependencies are shared')
    .action(checkShadedPackages);

  await program.parseAsync(process.argv);
}

await main();
