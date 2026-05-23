import {expect} from 'chai';
import {
  buildComparisonReport,
  compareModuleVersions,
  parseVersionsToml,
} from '../../scripts/lib/dependency-versions.mjs';
import {runDiagnosis} from '../../scripts/lib/diagnose-app-lib.mjs';

describe('diagnose-app', function () {
  it('parseVersionsToml', function () {
    const versions = parseVersionsToml(`
[versions]
composeUiTest = "1.11.2"
espresso = "3.7.0"
`);
    expect(versions.composeUiTest).to.equal('1.11.2');
    expect(versions.espresso).to.equal('3.7.0');
  });

  it('compareModuleVersions', function () {
    expect(compareModuleVersions('1.11.2', '1.11.2')).to.equal('equal');
    expect(compareModuleVersions('1.11.2', '1.10.0')).to.equal('minor');
    expect(compareModuleVersions('1.11.2', '2.0.0')).to.equal('major');
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
    expect(internet?.status).to.equal('fail');
    expect(report.ready).to.be.false;
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
        gradleCorpus:
          'android.permission.INTERNET\ncompileSdk = 35\nandroidx.compose.ui:ui-test:1.11.2',
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
    expect(composeDep?.status).to.equal('pass');
    expect(report.ready).to.be.true;
  });

  it('buildComparisonReport suggests toolsVersions on minor drift', function () {
    const depReport = buildComparisonReport(
      {compose: '1.11.2'},
      {compose: ['1.10.0']},
      {proguardLikely: false, minifyEnabled: false},
    );
    const compose = depReport.modules.find((m) => m.id === 'compose');
    expect(compose?.diff).to.equal('minor');
    expect(compose?.recommendation.espressoBuildConfig).to.eql({
      toolsVersions: {composeVersion: '1.10.0'},
    });
  });
});
