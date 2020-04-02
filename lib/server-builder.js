import { SubProcess } from 'teen_process';
import { fs, logger, system } from 'appium-support';
import _ from 'lodash';
import log from './logger';
import path from 'path';
import { EOL } from 'os';

const GRADLE_VERSION_KEY = 'gradle';
const GRADLE_URL_PREFIX = 'distributionUrl=';
const GRADLE_URL_TEMPLATE = 'https\\://services.gradle.org/distributions/gradle-VERSION-all.zip';
const GRADLE_MAX_ERROR_LOG_LINES = 15;

const GRADLE_DEPENDENCIES_PLACEHOLDER = '// additionalAppDependencies placeholder (don\'t change or delete this line)';

const VERSION_KEYS = [
  GRADLE_VERSION_KEY,
  'androidGradlePlugin',
  'compileSdk',
  'buildTools',
  'minSdk',
  'targetSdk',
  'kotlin'
];

const gradleLog = logger.getLogger('Gradle');

function buildServerSigningConfig (args) {
  return {
    zipAlign: true,
    keystoreFile: args.keystoreFile,
    keystorePassword: args.keystorePassword,
    keyAlias: args.keyAlias,
    keyPassword: args.keyPassword
  };
}

class ServerBuilder {
  constructor (args = {}) {
    this.serverPath = args.serverPath;
    this.showGradleLog = args.showGradleLog;

    const buildConfiguration = args.buildConfiguration || {};

    const versionConfiguration = buildConfiguration.toolsVersions || {};
    this.serverVersions = _.reduce(versionConfiguration, (acc, value, key) => {
      if (VERSION_KEYS.includes(key)) {
        acc[key] = value;
      } else {
        log.warn(`Got unexpected '${key}' in toolsVersion block of the build configuration`);
      }
      return acc;
    }, {});

    this.testAppPackage = args.testAppPackage;
    this.signingConfig = args.signingConfig;

    this.additionalAppDependencies = buildConfiguration.additionalAppDependencies || [];
  }

  async build () {
    if (this.serverVersions[GRADLE_VERSION_KEY]) {
      await this.setGradleWrapperVersion(this.serverVersions[GRADLE_VERSION_KEY]);
    }

    if (!Array.isArray(this.additionalAppDependencies)) {
      throw new Error('additionalAppDependencies must be an array');
    }
    if (!_.isEmpty(this.additionalAppDependencies)) {
      await this.insertAdditionalAppDependencies(this.additionalAppDependencies);
    }

    await this.runBuildProcess();
  }

  getCommand () {
    const cmd = system.isWindows() ? 'gradlew.bat' : './gradlew';
    const buildProperty = (key, value) => value ? `-P${key}=${value}` : null;
    let args = VERSION_KEYS
      .filter((key) => key !== GRADLE_VERSION_KEY)
      .map((key) => {
        const serverVersion = this.serverVersions[key];
        const gradleProperty = `appium${key.charAt(0).toUpperCase()}${key.slice(1)}`;
        return buildProperty(gradleProperty, serverVersion);
      })
      .filter(Boolean);

    if (this.signingConfig) {
      args.push(...(
        _.keys(this.signingConfig)
        .map((key) => [`appium${_.upperFirst(key)}`, this.signingConfig[key]])
        .map(([k, v]) => buildProperty(k, v))
        .filter(Boolean)
      ));
    }

    if (this.testAppPackage) {
      args.push(buildProperty('appiumTargetPackage', this.testAppPackage));
    }
    args.push('assembleAndroidTest');

    return {cmd, args};
  }

  async setGradleWrapperVersion (version) {
    const propertiesPath = path.resolve(this.serverPath, 'gradle', 'wrapper', 'gradle-wrapper.properties');
    const originalProperties = await fs.readFile(propertiesPath, 'utf8');
    const newProperties = this.updateGradleDistUrl(originalProperties, version);
    await fs.writeFile(propertiesPath, newProperties, 'utf8');
  }

  updateGradleDistUrl (propertiesContent, version) {
    return propertiesContent.replace(
      new RegExp(`^(${_.escapeRegExp(GRADLE_URL_PREFIX)}).+$`, 'gm'),
      `$1${GRADLE_URL_TEMPLATE.replace('VERSION', version)}`
    );
  }

  async insertAdditionalAppDependencies (additionalAppDependencies) {
    const buildPath = path.resolve(this.serverPath, 'app', 'build.gradle');
    const originalConfiguration = await fs.readFile(buildPath, 'utf8');
    const newConfiguration = this.updateDependencyLines(originalConfiguration, additionalAppDependencies);
    await fs.writeFile(buildPath, newConfiguration, 'utf8');
  }

  updateDependencyLines (configurationContent, additionalAppDependencies) {
    const dependencyLines = additionalAppDependencies
      .map(function (dependency) {
        // Disallow whitespace and quote characters that can break string literals in a patched build.gradle
        // and dollar characters that otherwise would be interpreted by String.replace below
        if (/[\s'\\$]/.test(dependency)) {
          throw new Error('Single quotes, dollar characters and whitespace characters' +
            ` are disallowed in additional dependencies: ${dependency}`);
        }
        return `implementation '${dependency}'`;
      })
      .join('$1'); // interpreted as Group 1 from the pattern below

    return configurationContent.replace(
      // Group 1 captures new line characters and indentation (eg. '\n\t') used in build.gradle file
      // This ensures that a patched build.gradle will have correct new lines and indentation
      new RegExp(`(\\s*^\\s*)${_.escapeRegExp(GRADLE_DEPENDENCIES_PLACEHOLDER)}\\s*$`, 'gm'),
      `$1${dependencyLines}`
    );
  }

  async runBuildProcess () {
    const {cmd, args} = this.getCommand();
    log.debug(`Beginning build with command '${cmd} ${args.join(' ')}' ` +
      `in directory '${this.serverPath}'`);
    const gradlebuild = new SubProcess(cmd, args, {
      cwd: this.serverPath,
      stdio: ['ignore', 'pipe', 'pipe'],
    });
    let buildLastLines = [];

    const logMsg = `Output from Gradle ${this.showGradleLog ? 'will' : 'will not'} be logged`;
    log.debug(`${logMsg}. To change this, use 'showGradleLog' desired capability`);
    gradlebuild.on('stream-line', (line) => {
      if (this.showGradleLog) {
        if (line.startsWith('[STDERR]')) {
          gradleLog.warn(line);
        } else {
          gradleLog.info(line);
        }
      }
      buildLastLines.push(`${EOL}${line}`);
      if (buildLastLines.length > GRADLE_MAX_ERROR_LOG_LINES) {
        buildLastLines = buildLastLines.slice(-GRADLE_MAX_ERROR_LOG_LINES);
      }
    });

    try {
      await gradlebuild.start();
      await gradlebuild.join();
    } catch (err) {
      let msg = `Unable to build Espresso server - ${err.message}\n` +
        `Gradle error message:${EOL}${buildLastLines}`;
      log.errorAndThrow(msg);
    }
  }
}

export { ServerBuilder, VERSION_KEYS, GRADLE_URL_TEMPLATE, GRADLE_DEPENDENCIES_PLACEHOLDER, buildServerSigningConfig };
export default ServerBuilder;
