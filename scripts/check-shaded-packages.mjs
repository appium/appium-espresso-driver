#!/usr/bin/env node
/**
 * Fails if the Espresso server test APK defines classes outside of the packages that are allowed to
 * keep their names (see `RelocateClassesTask` usage in espresso-server/app/build.gradle.kts).
 *
 * Usage: check-shaded-packages.mjs <apk> [--compose]
 */
import {execFileSync} from 'node:child_process';
import path from 'node:path';

import {logger} from 'appium/support.js';

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

const [apk, ...flags] = process.argv.slice(2);
if (!apk) {
  LOG.error('Usage: check-shaded-packages.mjs <apk> [--compose]');
  process.exit(2);
}

const sdkRoot = process.env.ANDROID_HOME || process.env.ANDROID_SDK_ROOT;
const apkanalyzer = process.env.APKANALYZER || path.join(sdkRoot ?? '', 'cmdline-tools', 'latest', 'bin', 'apkanalyzer');
const output = execFileSync(apkanalyzer, ['dex', 'packages', '--defined-only', apk], {
  encoding: 'utf8',
  maxBuffer: 1 << 28,
});

const allowed = [...ALWAYS_ALLOWED, ...(flags.includes('--compose') ? COMPOSE_ALLOWED : [])];
/** @param {string} pkg */
const isAllowed = (pkg) => EXACT_PACKAGES.includes(pkg) || allowed.some((a) => pkg === a || pkg.startsWith(`${a}.`));
// The listing is a tree where every package carries the counts of its subpackages too
const packages = new Map(
  output
    .split('\n')
    .map((line) => line.split('\t'))
    .filter(([kind, , , name]) => kind?.startsWith('P') && name && name !== '<TOTAL>')
    .map(([, count, , name]) => [name, Number(count)]),
);
const violations = [...packages]
  .map(([name, count]) => {
    const children = [...packages].filter(([n]) => n.startsWith(`${name}.`) && !n.slice(name.length + 1).includes('.'));
    return {name, own: count - children.reduce((sum, [, c]) => sum + c, 0)};
  })
  .filter(({name, own}) => own > 0 && !isAllowed(name));

if (violations.length > 0) {
  LOG.error(
    `The APK defines classes outside of the allowed packages (${allowed.join(', ')}):\n` +
      violations.map(({name, own}) => `  ${name} (${own})`).join('\n'),
  );
  process.exit(1);
}
LOG.info('OK: all the packages defined by the APK are allowed');
