import assert from 'node:assert/strict';
import {describe, it} from 'node:test';

import {system} from 'appium/support.js';

import {GRADLE_URL_TEMPLATE, VERSION_KEYS, updateDependencyLines} from '../../lib/commands/server/builder.js';
import {ServerBuilder} from '../../lib/commands/server/index.js';
import {log} from '../../lib/logger.js';

describe('server-builder', function () {
  describe('getCommand', function () {
    const expectedCmd = system.isWindows() ? 'gradlew.bat' : '/path/to/project/gradlew';

    it('should not pass properties when no versions are specified', function () {
      const expected = {cmd: expectedCmd, args: ['app:assembleAndroidTest']};
      assert.deepStrictEqual((new ServerBuilder(log, {serverPath: '/path/to/project'}) as any).getCommand(), expected);
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
      assert.deepStrictEqual((serverBuilder as any).getCommand(), expected);
    });

    it('should skip unknown version keys', function () {
      const unknownKey = 'unknown_key';
      assert.ok(!(VERSION_KEYS as readonly string[]).includes(unknownKey));

      const expected = {cmd: expectedCmd, args: ['app:assembleAndroidTest']};
      const serverBuilder = new ServerBuilder(log, {
        buildConfiguration: {
          toolsVersions: {
            [unknownKey]: '1.2.3',
          },
        },
        serverPath: '/path/to/project',
      });
      assert.deepStrictEqual((serverBuilder as any).getCommand(), expected);
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
      assert.deepStrictEqual((serverBuilder as any).getCommand(), expected);
    });

    it('should pass appiumComposeSupport=false when composeSupport is false', function () {
      const expected = {
        cmd: expectedCmd,
        args: ['-PappiumComposeSupport=false', 'app:assembleAndroidTest'],
      };
      const serverBuilder = new ServerBuilder(log, {
        buildConfiguration: {
          composeSupport: false,
        },
        serverPath: '/path/to/project',
      });
      assert.deepStrictEqual((serverBuilder as any).getCommand(), expected);
    });

    it('should not pass compose support property when composeSupport is true or omitted', function () {
      const expected = {cmd: expectedCmd, args: ['app:assembleAndroidTest']};
      assert.deepStrictEqual(
        (
          new ServerBuilder(log, {
            buildConfiguration: {composeSupport: true},
            serverPath: '/path/to/project',
          }) as any
        ).getCommand(),
        expected,
      );
      assert.deepStrictEqual((new ServerBuilder(log, {serverPath: '/path/to/project'}) as any).getCommand(), expected);
    });
  });

  describe('setGradleWrapperVersion', function () {
    const serverPath = 'server';
    it('should set correct URL in gradle.properties', function () {
      const readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      const serverBuilder = new ServerBuilder(log, {serverPath});
      const actualFileContent = (serverBuilder as any).updateGradleDistUrl(readFileResult, '1.2.3');

      assert.strictEqual(
        actualFileContent,
        `foo=1\ndistributionUrl=${GRADLE_URL_TEMPLATE.replace('VERSION', '1.2.3')}\nbar=2`,
      );
      assert.ok(actualFileContent.includes('gradle-1.2.3-all.zip'));
    });

    it('should keep other lines not affected', function () {
      const readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      const serverBuilder = new ServerBuilder(log, {serverPath});
      const actualFileContent = (serverBuilder as any).updateGradleDistUrl(readFileResult, '1.2.3');

      assert.match(actualFileContent, /^foo=1$/m);
      assert.match(actualFileContent, /^bar=2$/m);
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
      assert.strictEqual(
        replacedContent,
        `dependencies {
  ext.annotation_version = '1.1.0'

  implementation fileTree(dir: 'libs', include: ['*.jar'])

  // additionalAppDependencies placeholder (don't change or delete this line)
  a.b.c:1.2.3
  foo.bar.foobar:4.5.6

  testImplementation "org.powermock:powermock-api-mockito2:$mocklib_version"

  androidTestImplementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version"

  // additionalAndroidTestDependencies placeholder (don't change or delete this line)
}`,
      );

      const replacedContent2 = updateDependencyLines(replacedContent, 'additionalAndroidTestDependencies', [
        'a.b.c:1.2.3',
        'foo.bar.foobar:4.5.6',
      ]);
      assert.strictEqual(
        replacedContent2,
        `dependencies {
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
}`,
      );
    });

    it('should throw on single quotes in additional dependencies', async function () {
      const serverBuilder = new ServerBuilder(log, {serverPath});
      (serverBuilder as any).additionalAppDependencies = ["foo.':1.2.3"];

      await assert.rejects(
        (serverBuilder as any).insertAdditionalDependencies(),
        /Single quotes, dollar characters and whitespace characters are disallowed in additional dependencies/,
      );
    });

    it('should throw on dollar characters in additional dependencies', async function () {
      const serverBuilder = new ServerBuilder(log, {serverPath});
      (serverBuilder as any).additionalAndroidTestDependencies = ["foo.':1.2.3"];

      await assert.rejects(
        (serverBuilder as any).insertAdditionalDependencies(),
        /Single quotes, dollar characters and whitespace characters are disallowed in additional dependencies/,
      );
    });

    it('should throw on new lines in additional dependencies', async function () {
      const serverBuilder = new ServerBuilder(log, {serverPath});
      (serverBuilder as any).additionalAppDependencies = ['foo.\n:1.2.3'];

      await assert.rejects(
        (serverBuilder as any).insertAdditionalDependencies(),
        /Single quotes, dollar characters and whitespace characters are disallowed in additional dependencies/,
      );
    });
  });
});
