/**
 * @param {import('../types.mjs').AppInput} appInput
 * @returns {import('../types.mjs').DiagnosticCheck[]}
 */
export function checkInitializationProvider(appInput) {
  if (appInput.kind === 'apk') {
    return [];
  }
  const corpus = `${appInput.gradleCorpus ?? ''}\n${(appInput.manifestPaths ?? []).join('\n')}`;
  if (!/InitializationProvider/.test(corpus)) {
    return [
      {
        id: 'startup-provider',
        title: 'App Startup provider',
        status: 'pass',
        message: 'androidx.startup.InitializationProvider not found in scanned manifests.',
      },
    ];
  }
  if (/tools:node\s*=\s*["']remove["']/.test(corpus) && /InitializationProvider/.test(corpus)) {
    return [
      {
        id: 'startup-provider',
        title: 'App Startup provider',
        status: 'pass',
        message: 'InitializationProvider is explicitly removed for instrumentation (recommended for Espresso fixtures).',
      },
    ];
  }
  return [
    {
      id: 'startup-provider',
      title: 'App Startup provider',
      status: 'warn',
      message:
        'InitializationProvider is present without tools:node="remove". Under instrumentation this can cause Resources$NotFoundException for androidx_startup. Remove the provider or add startup-runtime to the AUT.',
      docRef: 'docs/compose-troubleshooting.md',
    },
  ];
}
