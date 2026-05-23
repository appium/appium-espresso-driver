/**
 * @param {import('../types.mjs').AppInput} appInput
 * @returns {import('../types.mjs').DiagnosticCheck}
 */
export function checkPrecompileInputKind(appInput) {
  if (appInput.kind === 'project') {
    return {
      id: 'input-gradle-project',
      title: 'App input',
      status: 'pass',
      message: 'Gradle project root — static checks can inspect manifests and build files.',
    };
  }
  return {
    id: 'input-gradle-project',
    title: 'App input',
    status: 'info',
    message:
      'APK input: scans the built artifact (permissions, dependencies, obfuscation hints). Gradle sources are optional for extra manifest and build-file checks.',
  };
}
