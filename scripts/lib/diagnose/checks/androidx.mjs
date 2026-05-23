/**
 * @param {import('../types.mjs').AppInput} appInput
 * @returns {import('../types.mjs').DiagnosticCheck[]}
 */
export function checkAndroidX(appInput) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const usesAndroidX = /android\.useAndroidX\s*=\s*true/i.test(appInput.gradleCorpus ?? '');
  if (usesAndroidX) {
    return [
      {
        id: 'androidx-migration',
        title: 'AndroidX',
        status: 'pass',
        message: 'android.useAndroidX=true is set in gradle.properties.',
      },
    ];
  }
  if (/com\.android\.support\./.test(appInput.gradleCorpus ?? '')) {
    return [
      {
        id: 'androidx-migration',
        title: 'AndroidX',
        status: 'fail',
        message:
          'Legacy Android Support Library references detected without android.useAndroidX=true. Migrate the AUT to AndroidX before embedding the Espresso server.',
      },
    ];
  }
  return [
    {
      id: 'androidx-migration',
      title: 'AndroidX',
      status: 'warn',
      message:
        'android.useAndroidX=true not found in scanned gradle.properties files. Confirm the AUT uses AndroidX artifacts.',
    },
  ];
}
