import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { system } from 'appium-support';
import {
  GRADLE_DEPENDENCIES_PLACEHOLDER,
  GRADLE_URL_TEMPLATE,
  ServerBuilder,
  VERSION_KEYS
} from '../../lib/server-builder';

chai.should();
chai.use(chaiAsPromised);
const expect = chai.expect;

describe('server-builder', function () {
  describe('getCommand', function () {
    const expectedCmd = system.isWindows() ? 'gradlew.bat' : './gradlew';

    it('should not pass properties when no versions are specified', function () {
      const expected = {cmd: expectedCmd, args: ['assembleAndroidTest']};
      new ServerBuilder().getCommand().should.eql(expected);
    });

    it('should pass only specified versions as properties and pass them correctly', function () {
      const expected = {cmd: expectedCmd, args: ['-PappiumAndroidGradlePlugin=1.2.3', 'assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({
        buildConfiguration: {
          toolsVersions: {
            androidGradlePlugin: '1.2.3'
          }
        }
      });
      serverBuilder.getCommand().should.eql(expected);
    });

    it('should skip unknown version keys', function () {
      const unknownKey = 'unknown_key';
      VERSION_KEYS.should.not.contain(unknownKey);

      const expected = {cmd: expectedCmd, args: ['assembleAndroidTest']};
      const serverBuilder = new ServerBuilder({
        buildConfiguration: {
          toolsVersions: {
            [unknownKey]: '1.2.3'
          }
        }
      });
      serverBuilder.getCommand().should.eql(expected);
    });

    it('should not pass gradle_version as property', function () {
      const expected = {cmd: expectedCmd, args: ['assembleAndroidTest']};
      const serverBuilder = new ServerBuilder({
        buildConfiguration: {
          toolsVersions: {
            gradle_version: '1.2.3'
          }
        }
      });
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

  describe('updateDependencyLines', function () {
    const serverPath = 'server';
    const readFileResult = 'foo=1\n' + GRADLE_DEPENDENCIES_PLACEHOLDER + '\nbar=2';
    const goodDependencies = [
      'a.b.c:1.2.3',
      'foo.bar.foobar:4.5.6'
    ];
    it('should generate correct lines in build.gradle', function () {
      let serverBuilder = new ServerBuilder({serverPath});
      let actualFileContent = serverBuilder.updateDependencyLines(readFileResult, goodDependencies);

      actualFileContent.should.eql(
        'foo=1\n' +
        'implementation \'a.b.c:1.2.3\'\n' +
        'implementation \'foo.bar.foobar:4.5.6\'\n' +
        'bar=2'
      );
    });

    it('should throw on single quotes in additional dependencies', function () {
      let serverBuilder = new ServerBuilder({serverPath});

      expect(function () {
        serverBuilder.updateDependencyLines(readFileResult, ['foo.\':1.2.3']);
      }).to.throw(/Disallowed characters in an additional dependency/);
    });

    it('should throw on new lines in additional dependencies', function () {
      let serverBuilder = new ServerBuilder({serverPath});

      expect(function () {
        serverBuilder.updateDependencyLines(readFileResult, ['foo.\n:1.2.3']);
      }).to.throw(/Disallowed characters in an additional dependency/);
    });

    it('should throw when additional dependencies is not an array', function () {
      let serverBuilder = new ServerBuilder({serverPath});

      expect(function () {
        serverBuilder.updateDependencyLines(readFileResult, 'string');
      }).to.throw('additionalAppDependencies must be an array');
    });

    it('should keep other lines not affected', function () {
      let serverBuilder = new ServerBuilder({serverPath});
      let actualFileContent = serverBuilder.updateDependencyLines(readFileResult, goodDependencies);

      actualFileContent.should.match(/^foo=1$/m);
      actualFileContent.should.match(/^bar=2$/m);
    });
  });
});
