/**
 * @param {import('../types.mjs').AppInput} appInput
 * @returns {Promise<import('../types.mjs').DiagnosticCheck[]>}
 */
export async function checkInternetPermission(appInput) {
  if (appInput.kind === 'apk') {
    if (appInput.apkHasInternetPermission === true) {
      return [
        {
          id: 'manifest-internet',
          title: 'INTERNET permission',
          status: 'pass',
          message: 'APK declares android.permission.INTERNET (required for the Espresso HTTP server).',
        },
      ];
    }
    if (appInput.apkHasInternetPermission === false) {
      return [
        {
          id: 'manifest-internet',
          title: 'INTERNET permission',
          status: 'fail',
          message:
            'APK does not declare android.permission.INTERNET. Add <uses-permission android:name="android.permission.INTERNET" /> to the AUT manifest and reinstall.',
          docRef: 'README.md#troubleshooting',
        },
      ];
    }
    return [
      {
        id: 'manifest-internet',
        title: 'INTERNET permission',
        status: 'info',
        message:
          'Could not verify INTERNET on the APK (set ANDROID_SDK_ROOT and install Android build-tools so aapt2 is available).',
      },
    ];
  }

  const corpus = appInput.gradleCorpus ?? '';
  const manifests = appInput.manifestPaths ?? [];
  const hasInternet =
    /android\.permission\.INTERNET/.test(corpus) ||
    manifests.some((m) => /android\.permission\.INTERNET/.test(m));

  if (hasInternet) {
    return [
      {
        id: 'manifest-internet',
        title: 'INTERNET permission',
        status: 'pass',
        message:
          'AUT manifest includes android.permission.INTERNET (required — the server socket runs in the app process).',
      },
    ];
  }
  return [
    {
      id: 'manifest-internet',
      title: 'INTERNET permission',
      status: 'fail',
      message:
        'No android.permission.INTERNET found under app/src/main/AndroidManifest.xml (or module manifests). Add it to the AUT manifest and bump versionCode before reinstalling.',
      docRef: 'README.md#troubleshooting',
    },
  ];
}
