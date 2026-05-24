/** @typedef {'pass' | 'warn' | 'fail' | 'info' | 'skip'} CheckStatus */

/**
 * @typedef {Object} DiagnosticCheck
 * @property {string} id
 * @property {string} title
 * @property {CheckStatus} status
 * @property {string} message
 * @property {Record<string, unknown>} [espressoBuildConfig]
 * @property {string} [docRef]
 */

/**
 * Driver and espresso-server defaults loaded for dependency and static checks.
 * @typedef {Object} EspressoServerDefaults
 * @property {string} driverVersion
 * @property {string | null} compileSdk
 * @property {string | null} minSdk
 * @property {Record<string, string>} versions
 */

/**
 * Normalized application input for diagnosis (Gradle project or APK).
 * @typedef {Object} AppInput
 * @property {'apk' | 'project'} kind
 * @property {string} path
 * @property {Record<string, string[]>} versions
 * @property {boolean} proguardLikely
 * @property {boolean | null} minifyEnabled
 * @property {string[]} sources
 * @property {string} [gradleCorpus]
 * @property {string[]} [manifestPaths]
 * @property {boolean | null} [apkHasInternetPermission]
 */

/**
 * @typedef {Object} DiagnosisReport
 * @property {boolean} ready
 * @property {number} failCount
 * @property {number} warnCount
 * @property {DiagnosticCheck[]} checks
 * @property {import('../dependency-versions/types.mjs').ComparisonReport} dependencyReport
 * @property {EspressoServerDefaults} serverDefaults
 * @property {AppInput} input
 * @property {Record<string, unknown>} mergedEspressoBuildConfig
 * @property {string} summary
 */

export {};
