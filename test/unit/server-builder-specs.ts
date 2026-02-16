import {system} from 'appium/support';
import {GRADLE_URL_TEMPLATE, ServerBuilder, VERSION_KEYS} from '../../lib/server-builder';
import {updateDependencyLines} from '../../lib/utils';
import {log} from '../../lib/logger';
import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';

const {expect} = chai;
chai.use(chaiAsPromised);

describe('server-builder', function () {
  describe('getCommand', function () {
    const expectedCmd = system.isWindows() ? 'gradlew.bat' : '/path/to/project/gradlew';

    it('should not pass properties when no versions are specified', function () {
      const expected = {cmd: expectedCmd, args: ['app:assembleAndroidTest']};
      expect((new ServerBuilder(log, {serverPath: '/path/to/project'}) as any).getCommand()).to.eql(
        expected,
      );
    });

    it('should pass only specified versions as properties and pass them correctly', function () {
      const expected = {
        cmd: expectedCmd,
        args: ['-PappiumAndroidGradlePlugin=1.2.3', 'app:assembleAndroidTest'],
      };
      const serverBuilder = new ServerBuilder(log, {
        buildConfiguration: {
          toolsVersions: {
            androidGradlePlugin: '1.2.3',
          },
        },
        serverPath: '/path/to/project',
      });
      expect((serverBuilder as any).getCommand()).to.eql(expected);
    });

    it('should skip unknown version keys', function () {
      const unknownKey = 'unknown_key';
      expect(VERSION_KEYS).to.not.contain(unknownKey);

      const expected = {cmd: expectedCmd, args: ['app:assembleAndroidTest']};
      const serverBuilder = new ServerBuilder(log, {
        buildConfiguration: {
          toolsVersions: {
            [unknownKey]: '1.2.3',
          },
        },
        serverPath: '/path/to/project',
      });
      expect((serverBuilder as any).getCommand()).to.eql(expected);
    });

    it('should not pass gradle_version as property', function () {
      const expected = {cmd: expectedCmd, args: ['app:assembleAndroidTest']};
      const serverBuilder = new ServerBuilder(log, {
        buildConfiguration: {
          toolsVersions: {
            gradle_version: '1.2.3',
          },
        },
        serverPath: '/path/to/project',
      });
      expect((serverBuilder as any).getCommand()).to.eql(expected);
    });
  });

  describe('setGradleWrapperVersion', function () {
    const serverPath = 'server';
    it('should set correct URL in gradle.properties', function () {
      const readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      const serverBuilder = new ServerBuilder(log, {serverPath});
      const actualFileContent = (serverBuilder as any).updateGradleDistUrl(readFileResult, '1.2.3');

      expect(actualFileContent).to.eql(
        `foo=1\ndistributionUrl=${GRADLE_URL_TEMPLATE.replace('VERSION', '1.2.3')}\nbar=2`,
      );
      expect(actualFileContent).to.contain('gradle-1.2.3-all.zip');
    });

    it('should keep other lines not affected', function () {
      const readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      const serverBuilder = new ServerBuilder(log, {serverPath});
      const actualFileContent = (serverBuilder as any).updateGradleDistUrl(readFileResult, '1.2.3');

      expect(actualFileContent).to.match(/^foo=1$/m);
      expect(actualFileContent).to.match(/^bar=2$/m);
    });
  });

  describe('insertAdditionalDependencies', function () {
    const serverPath = 'server';
    it('should generate correct content and keep current indent in build.gradle.kts', function () {
      const gradleContent = `dependencies {
  ext.annotation_version = '1.1.0'

  implementation fileTree(dir: 'libs', include: ['*.jar'])

  // additionalAppDependencies placeholder (don't change or delete this line)

  testImplementation "org.powermock:powermock-api-mockito2:$mocklib_version"

  androidTestImplementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"

  // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}`;
      const replacedContent = updateDependencyLines(gradleContent, 'additionalAppDependencies', [
        'a.b.c:1.2.3',
        'foo.bar.foobar:4.5.6',
      ]);
      expect(replacedContent).to.eql(`dependencies {
  ext.annotation_version = '1.1.0'

  implementation fileTree(dir: 'libs', include: ['*.jar'])

  // additionalAppDependencies placeholder (don't change or delete this line)
  a.b.c:1.2.3
  foo.bar.foobar:4.5.6

  testImplementation "org.powermock:powermock-api-mockito2:$mocklib_version"

  androidTestImplementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"

  // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}`);

      const replacedContent2 = updateDependencyLines(
        replacedContent,
        'additionalAndroidTestDependencies',
        ['a.b.c:1.2.3', 'foo.bar.foobar:4.5.6'],
      );
      expect(replacedContent2).to.eql(`dependencies {
  ext.annotation_version = '1.1.0'

  implementation fileTree(dir: 'libs', include: ['*.jar'])

  // additionalAppDependencies placeholder (don't change or delete this line)
  a.b.c:1.2.3
  foo.bar.foobar:4.5.6

  testImplementation "org.powermock:powermock-api-mockito2:$mocklib_version"

  androidTestImplementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"

  // additionalAndroidTestDependencies placeholder (don't change or delete this line)
  a.b.c:1.2.3
  foo.bar.foobar:4.5.6
}`);
    });

    it('should throw on single quotes in additional dependencies', async function () {
      const serverBuilder = new ServerBuilder(log, {serverPath});
      (serverBuilder as any).additionalAppDependencies = ["foo.':1.2.3"];

      await expect((serverBuilder as any).insertAdditionalDependencies()).to.be.rejectedWith(
        /Single quotes, dollar characters and whitespace characters are disallowed in additional dependencies/,
      );
    });

    it('should throw on dollar characters in additional dependencies', async function () {
      const serverBuilder = new ServerBuilder(log, {serverPath});
      (serverBuilder as any).additionalAndroidTestDependencies = ["foo.':1.2.3"];

      await expect((serverBuilder as any).insertAdditionalDependencies()).to.be.rejectedWith(
        /Single quotes, dollar characters and whitespace characters are disallowed in additional dependencies/,
      );
    });

    it('should throw on new lines in additional dependencies', async function () {
      const serverBuilder = new ServerBuilder(log, {serverPath});
      (serverBuilder as any).additionalAppDependencies = ['foo.\n:1.2.3'];

      await expect((serverBuilder as any).insertAdditionalDependencies()).to.be.rejectedWith(
        /Single quotes, dollar characters and whitespace characters are disallowed in additional dependencies/,
      );
    });
  });
});
