/** @typedef {'equal' | 'patch' | 'minor' | 'major' | 'unknown' | 'present' | 'absent'} VersionDiffKind */

/**
 * @typedef {Object} Recommendation
 * @property {'ok' | 'info' | 'suggestion' | 'warning'} level
 * @property {string} message
 * @property {Record<string, unknown>} [espressoBuildConfig]
 * @property {boolean} [preferAppUpdate]
 */

/**
 * @typedef {Object} TrackedModule
 * @property {string} id
 * @property {string} label
 * @property {string | null} toolsVersionKey
 * @property {string} catalogKey
 * @property {string} gradleProperty
 * @property {boolean} [testOnly] When true, only warn if detected in the AUT (no version comparison).
 * @property {RegExp[]} patterns
 */

/**
 * @typedef {TrackedModule & {
 *   serverVersion: string | null,
 *   appVersions: string[],
 *   appVersion: string | null,
 *   diff: VersionDiffKind,
 *   recommendation: Recommendation,
 * }} ModuleComparison
 */

/**
 * @typedef {Object} ComparisonReport
 * @property {boolean} proguardLikely
 * @property {boolean | null} minifyEnabled
 * @property {ModuleComparison[]} modules
 * @property {string} summary
 */

export {};
