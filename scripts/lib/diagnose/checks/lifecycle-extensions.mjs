/**
 * @param {import('../types.mjs').AppInput} appInput
 * @returns {import('../types.mjs').DiagnosticCheck[]}
 */
export function checkLifecycleExtensionsPin(appInput) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const corpus = appInput.gradleCorpus ?? '';
  if (!/lifecycle-extensions/.test(corpus)) {
    return [];
  }
  return [
    {
      id: 'lifecycle-extensions',
      title: 'Legacy lifecycle-extensions',
      status: 'warn',
      message:
        'lifecycle-extensions detected. Avoid pinning it in additionalAndroidTestDependencies — it often causes ProcessLifecycleOwner / lifecycle NoSuchMethodError with modern Compose.',
      docRef: 'docs/compose-troubleshooting.md',
    },
  ];
}
