import assert from 'node:assert/strict';
import path from 'node:path';
import {describe, it} from 'node:test';

import {fs, tempDir} from 'appium/support.js';

import {
  buildComparisonReport,
  collectAppVersionsFromProject,
  compareModuleVersions,
  extractMainAppDependencyScopes,
  mapMetaInfVersionBaseToModule,
  mergeMetaInfEmbeddedVersions,
  parseKotlinMetadataVersionsFromDexdump,
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

  it('runDiagnosis passes aligned kotlin versions without test libraries', async function () {
    const report = await runDiagnosis(
      {
        kind: 'project',
        path: '/fake',
        versions: {kotlin: ['2.0.0']},
        proguardLikely: false,
        minifyEnabled: false,
        sources: [],
        gradleCorpus: 'android.permission.INTERNET\ncompileSdk = 35\nkotlin = "2.0.0"',
        manifestPaths: ['<uses-permission android:name="android.permission.INTERNET" />'],
        apkHasInternetPermission: null,
      },
      {
        driverVersion: '8.5.6',
        compileSdk: '35',
        minSdk: '26',
        versions: {kotlin: '2.0.0', espresso: '3.7.0'},
      },
    );
    const kotlinDep = report.checks.find((c) => c.id === 'dependency-kotlin');
    assert.strictEqual(kotlinDep?.status, 'pass');
    assert.strictEqual(report.ready, true);
  });

  it('mapMetaInfVersionBaseToModule maps Compose artifacts', function () {
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.compose.ui_ui'), 'compose');
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.compose.ui_ui-test'), 'compose');
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.annotation_annotation-experimental'), null);
    assert.strictEqual(mapMetaInfVersionBaseToModule('androidx.test.espresso.espresso-core'), 'espresso');
  });

  it('parseKotlinMetadataVersionsFromDexdump reads @Metadata mv', function () {
    const versions = parseKotlinMetadataVersionsFromDexdump(
      'VISIBILITY_RUNTIME Lkotlin/Metadata; k=3 mv={ 2 3 0 } xi=48',
    );
    assert.deepStrictEqual(versions, ['2.3.0']);
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

  it('buildComparisonReport suggests toolsVersions on minor kotlin drift', function () {
    const depReport = buildComparisonReport(
      {kotlin: '2.1.0'},
      {kotlin: ['2.0.0']},
      {proguardLikely: false, minifyEnabled: false},
    );
    const kotlin = depReport.modules.find((m) => m.id === 'kotlin');
    assert.strictEqual(kotlin?.diff, 'minor');
    assert.deepStrictEqual(kotlin?.recommendation.espressoBuildConfig, {
      toolsVersions: {kotlin: '2.0.0'},
    });
  });

  it('extractMainAppDependencyScopes drops androidTest configuration lines', function () {
    const mainOnly = extractMainAppDependencyScopes(`
dependencies {
    implementation("androidx.core:core:1.15.0")
    androidTestImplementation("androidx.compose.ui:ui-test:1.11.2")
    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.7.0",
    )
}
`);
    assert.ok(mainOnly.includes('androidx.core:core:1.15.0'));
    assert.ok(!mainOnly.includes('ui-test'));
    assert.ok(!mainOnly.includes('espresso-core'));
  });

  it('collectAppVersionsFromProject ignores test-only modules in androidTest configurations', async function () {
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
    await fs.mkdir(path.join(root, 'gradle'), {recursive: true});
    await fs.writeFile(
      path.join(root, 'gradle', 'libs.versions.toml'),
      `
[versions]
composeUiTest = "1.11.2"
espresso = "3.7.0"
`,
      'utf8',
    );
    const {versions} = await collectAppVersionsFromProject(root);
    assert.deepStrictEqual(versions.compose, ['1.11.2']);
    assert.deepStrictEqual(versions.espresso ?? [], []);
    await fs.rimraf(root);
  });

  it('collectAppVersionsFromProject detects test deps in implementation', async function () {
    const root = await tempDir.openDir();
    await fs.writeFile(
      path.join(root, 'build.gradle.kts'),
      `dependencies { implementation("androidx.test.espresso:espresso-core:3.6.1") }`,
      'utf8',
    );
    const {versions} = await collectAppVersionsFromProject(root);
    assert.deepStrictEqual(versions.espresso, ['3.6.1']);
    await fs.rimraf(root);
  });

  it('buildComparisonReport warns when test libraries are detected', function () {
    const depReport = buildComparisonReport(
      {espresso: '3.7.0'},
      {espresso: ['3.6.1']},
      {proguardLikely: false, minifyEnabled: false, detectionSource: 'project'},
    );
    const espresso = depReport.modules.find((m) => m.id === 'espresso');
    assert.strictEqual(espresso?.diff, 'present');
    assert.strictEqual(espresso?.recommendation.level, 'warning');
    assert.ok(espresso?.recommendation.message.includes('instrumented-test'));
    assert.strictEqual(espresso?.recommendation.espressoBuildConfig, undefined);
  });
});
