import assert from 'node:assert/strict';
import path from 'node:path';
import {describe, it} from 'node:test';

import {fs, tempDir} from 'appium/support.js';

import {
  buildComparisonReport,
  collectAppVersionsFromProject,
  compareModuleVersions,
  mapMetaInfVersionBaseToModule,
  mergeMetaInfEmbeddedVersions,
  parseVersionsToml,
} from '../../scripts/lib/dependency-versions/index.mjs';
import {runDiagnosis} from '../../scripts/lib/diagnose/index.mjs';

describe('diagnose-app', function () {
  it('parseVersionsToml', function () {
    const versions = parseVersionsToml(`
[versions]
composeUiTest = "1.11.2"
espresso = "3.7.0"
`);
    assert.strictEqual(versions.composeUiTest, '1.11.2');
    assert.strictEqual(versions.espresso, '3.7.0');
  });

  it('compareModuleVersions', function () {
    assert.strictEqual(compareModuleVersions('1.11.2', '1.11.2'), 'equal');
    assert.strictEqual(compareModuleVersions('1.11.2', '1.10.0'), 'minor');
    assert.strictEqual(compareModuleVersions('1.11.2', '2.0.0'), 'major');
  });

  it('runDiagnosis fails on missing INTERNET in manifest', async function () {
    const report = await runDiagnosis(
      {
        kind: 'project',
        path: '/fake',
        versions: {compose: ['1.11.2']},
        proguardLikely: false,
        minifyEnabled: false,
        sources: ['app/build.gradle.kts'],
        gradleCorpus: 'compileSdk = 35\nandroidx.compose.ui:ui-test:1.11.2',
        manifestPaths: ['<manifest package="com.example"></manifest>'],
        apkHasInternetPermission: null,
      },
      {
        driverVersion: '8.5.6',
        compileSdk: '35',
        minSdk: '26',
        versions: {compose: '1.11.2', espresso: '3.7.0'},
      },
    );
    const internet = report.checks.find((c) => c.id === 'manifest-internet');
    assert.strictEqual(internet?.status, 'fail');
    assert.strictEqual(report.ready, false);
  });

  it('runDiagnosis passes aligned compose versions', async function () {
    const report = await runDiagnosis(
      {
        kind: 'project',
        path: '/fake',
        versions: {compose: ['1.11.2']},
        proguardLikely: false,
        minifyEnabled: false,
        sources: [],
        gradleCorpus: 'android.permission.INTERNET\ncompileSdk = 35\nandroidx.compose.ui:ui-test:1.11.2',
        manifestPaths: ['<uses-permission android:name="android.permission.INTERNET" />'],
        apkHasInternetPermission: null,
      },
      {
        driverVersion: '8.5.6',
        compileSdk: '35',
        minSdk: '26',
        versions: {compose: '1.11.2', espresso: '3.7.0'},
      },
    );
    const composeDep = report.checks.find((c) => c.id === 'dependency-compose');
    assert.strictEqual(composeDep?.status, 'pass');
    assert.strictEqual(report.ready, true);
  });

  it('mapMetaInfVersionBaseToModule maps Compose artifacts', function () {
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.compose.ui_ui'), 'compose');
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.compose.ui_ui-test'), 'compose');
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.annotation_annotation-experimental'), null);
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.test.espresso.espresso-core'), null);
  });

  it('mergeMetaInfEmbeddedVersions reads AGP META-INF version files', async function () {
    const root = await tempDir.openDir();
    const metaDir = path.join(root, 'META-INF');
    await fs.mkdir(metaDir);
    await fs.writeFile(path.join(metaDir, 'androidx.compose.ui_ui.version'), '1.11.2\n', 'utf8');
    /** @type {Record<string, Set<string>>} */
    const found = {compose: new Set<string>()};
    await mergeMetaInfEmbeddedVersions(root, found);
    assert.deepStrictEqual([...found.compose], ['1.11.2']);
    await fs.rimraf(root);
  });

  it('buildComparisonReport suggests toolsVersions on minor compose drift', function () {
    const depReport = buildComparisonReport(
      {compose: '1.11.2'},
      {compose: ['1.10.0']},
      {proguardLikely: false, minifyEnabled: false},
    );
    const compose = depReport.modules.find((m) => m.id === 'compose');
    assert.strictEqual(compose?.diff, 'minor');
    assert.deepStrictEqual(compose?.recommendation.espressoBuildConfig, {
      toolsVersions: {composeVersion: '1.10.0'},
    });
  });

  it('collectAppVersionsFromProject reads Compose from Gradle files only', async function () {
    const root = await tempDir.openDir();
    const appDir = path.join(root, 'app');
    await fs.mkdir(path.join(appDir, 'src', 'main'), {recursive: true});
    await fs.writeFile(
      path.join(appDir, 'build.gradle.kts'),
      `
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    androidTestImplementation("androidx.compose.ui:ui-test:1.11.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
`,
      'utf8',
    );
    const {versions} = await collectAppVersionsFromProject(root);
    assert.deepStrictEqual(versions.compose, ['1.11.2']);
    assert.deepStrictEqual(Object.keys(versions), ['compose']);
    await fs.rimraf(root);
  });

  it('runDiagnosis does not check the libraries that the shaded server no longer shares', async function () {
    const report = await runDiagnosis(
      {
        kind: 'project',
        path: '/fake',
        versions: {},
        proguardLikely: false,
        minifyEnabled: false,
        sources: [],
        gradleCorpus:
          'android.permission.INTERNET\ncompileSdk = 35\nimplementation("androidx.test.espresso:espresso-core:3.6.1")\n' +
          'implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")\nimplementation("com.android.support:appcompat-v7:28.0.0")',
        manifestPaths: [
          '<uses-permission android:name="android.permission.INTERNET" /><provider android:name="androidx.startup.InitializationProvider" />',
        ],
        apkHasInternetPermission: null,
      },
      {driverVersion: '10.3.0', compileSdk: '35', minSdk: '26', versions: {compose: '1.11.2'}},
    );
    const ids = report.checks.map((c) => c.id);
    for (const id of [
      'dependency-espresso',
      'dependency-kotlin',
      'lifecycle-extensions',
      'androidx-migration',
      'startup-provider',
    ]) {
      assert.ok(!ids.includes(id), `${id} should not be reported`);
    }
    assert.strictEqual(report.ready, true);
  });
});
