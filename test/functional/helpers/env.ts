// Emulator API level under test; NaN when ANDROID_SDK_VERSION is not set (local runs)
export const apiLevel = parseInt(process.env.ANDROID_SDK_VERSION ?? '', 10);

// Chromedriver-backed specs need a recent WebView; see the e2e plan for the supported matrix
export const WEBVIEW_MIN_API = 36;

/** True only if the API level is known and lower than `level`, so unknown levels never skip. */
export function isApiBelow(level: number): boolean {
  return Number.isFinite(apiLevel) && apiLevel < level;
}

/** True only if the API level is known and equals `level`. */
export function isApi(level: number): boolean {
  return apiLevel === level;
}
