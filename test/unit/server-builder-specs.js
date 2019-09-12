import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { VERSION_KEYS, GRADLE_URL_TEMPLATE, ServerBuilder } from '../../lib/server-builder';
import path from 'path';

chai.should();
chai.use(chaiAsPromised);

describe('server-builder', function () {
  describe('getCommand', function () {
    const expected_cmd = `.${path.sep}gradlew`;

    it('should not pass properties when no versions are specified', function () {
      const expected = {cmd: expected_cmd, args: ['assembleAndroidTest']};
      new ServerBuilder().getCommand().should.deep.eql(expected);
    });

    it('should pass only specified versions as properties', function () {
      const expected = {cmd: expected_cmd, args: ['-Pagp_version=1.2.3', 'assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({versions: {agp_version: '1.2.3'}});
      serverBuilder.getCommand().should.deep.eql(expected);
    });

    it('should skip unknown version keys', function () {
      let unknownKey = 'unknown_key';
      VERSION_KEYS.should.not.contain(unknownKey);

      const expected = {cmd: expected_cmd, args: ['assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({versions: {[unknownKey]: '1.2.3'}});
      serverBuilder.getCommand().should.deep.eql(expected);
    });

    it('should not pass gradle_version as property', function () {
      const expected = {cmd: expected_cmd, args: ['assembleAndroidTest']};
      let serverBuilder = new ServerBuilder({versions: {gradle_version: '1.2.3'}});
      serverBuilder.getCommand().should.deep.eql(expected);
    });
  });

  describe('setGradleWrapperVersion', function () {
    let serverPath = 'server';
    let readFileResult = '';

    it('should set correct URL in gradle.properties', function () {
      readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      let serverBuilder = new ServerBuilder({serverPath});
      let actualFileContent = serverBuilder.updateGradleDistUrl(readFileResult, '1.2.3');

      actualFileContent.should.eql(
        `foo=1\ndistributionUrl=${GRADLE_URL_TEMPLATE.replace('VERSION', '1.2.3')}\nbar=2`
      );
      actualFileContent.should.contain('gradle-1.2.3-all.zip');
    });

    it('should keep other lines not affected', function () {
      readFileResult = 'foo=1\ndistributionUrl=abc\nbar=2';
      let serverBuilder = new ServerBuilder({serverPath});
      let actualFileContent = serverBuilder.updateGradleDistUrl(readFileResult, '1.2.3');

      actualFileContent.should.match(/^foo=1$/m);
      actualFileContent.should.match(/^bar=2$/m);
    });
  });
});
