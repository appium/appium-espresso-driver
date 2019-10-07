import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { system } from 'appium-support';
import { VERSION_KEYS, GRADLE_URL_TEMPLATE, ServerBuilder } from '../../lib/server-builder';

chai.should();
chai.use(chaiAsPromised);

describe('server-builder', function () {
  describe('getCommand', function () {
    const expectedCmd = system.isWindows() ? 'gradlew.bat' : './gradlew';

    it('should not pass properties when no versions are specified', function () {
      const expected = {cmd: expectedCmd, args: ['assembleAndroidTest']};
      new ServerBuilder().getCommand().should.eql(expected);
    });

    it('should pass only specified versions as properties', function () {
      const expected = {cmd: expectedCmd, args: ['-Pandroid_gradle_plugin_version=1.2.3', 'assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({versions: {android_gradle_plugin_version: '1.2.3'}});
      serverBuilder.getCommand().should.eql(expected);
    });

    it('should skip unknown version keys', function () {
      const unknownKey = 'unknown_key';
      VERSION_KEYS.should.not.contain(unknownKey);

      const expected = {cmd: expectedCmd, args: ['assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({versions: {[unknownKey]: '1.2.3'}});
      serverBuilder.getCommand().should.eql(expected);
    });

    it('should not pass gradle_version as property', function () {
      const expected = {cmd: expectedCmd, args: ['assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({versions: {gradle_version: '1.2.3'}});
      serverBuilder.getCommand().should.eql(expected);
    });
  });

  describe('setGradleWrapperVersion', function () {
    const serverPath = 'server';
    it('should set correct URL in gradle.properties', function () {
      const readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      let serverBuilder = new ServerBuilder({serverPath});
      let actualFileContent = serverBuilder.updateGradleDistUrl(readFileResult, '1.2.3');

      actualFileContent.should.eql(
        `foo=1\ndistributionUrl=${GRADLE_URL_TEMPLATE.replace('VERSION', '1.2.3')}\nbar=2`
      );
      actualFileContent.should.contain('gradle-1.2.3-all.zip');
    });

    it('should keep other lines not affected', function () {
      const readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      let serverBuilder = new ServerBuilder({serverPath});
      let actualFileContent = serverBuilder.updateGradleDistUrl(readFileResult, '1.2.3');

      actualFileContent.should.match(/^foo=1$/m);
      actualFileContent.should.match(/^bar=2$/m);
    });
  });
});
