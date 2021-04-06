import { SubProcess } from 'teen_process';
import { fs, logger, system } from 'appium-support';
import _ from 'lodash';
import log from './logger';
import path from 'path';
import { EOL } from 'os';
import { updateDependencyLines } from './utils';

const GRADLE_VERSION_KEY = 'gradle';
const GRADLE_URL_PREFIX = 'distributionUrl=';
const GRADLE_URL_TEMPLATE = 'https\\://services.gradle.org/distributions/gradle-VERSION-all.zip';
const GRADLE_MAX_ERROR_LOG_LINES = 15;
const DEPENDENCY_PROP_NAMES = ['additionalAppDependencies', 'additionalAndroidTestDependencies'];

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

    for (const propName of DEPENDENCY_PROP_NAMES) {
      this[propName] = buildConfiguration[propName] || [];
    }
  }

  async build () {
    if (this.serverVersions[GRADLE_VERSION_KEY]) {
      await this.setGradleWrapperVersion(this.serverVersions[GRADLE_VERSION_KEY]);
    }

    await this.insertAdditionalDependencies();

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
    args.push('app:assembleAndroidTest');

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

  async insertAdditionalDependencies () {
    let hasAdditionalDeps = false;
    for (const propName of DEPENDENCY_PROP_NAMES) {
      if (!_.isArray(this[propName])) {
        throw new Error(`'${propName}' must be an array`);
      }
      if (_.isEmpty(this[propName].filter((line) => _.trim(line)))) {
        continue;
      }

      for (const dep of this[propName]) {
        if (/[\s'\\$]/.test(dep)) {
          throw new Error('Single quotes, dollar characters and whitespace characters' +
            ` are disallowed in additional dependencies: ${dep}`);
        }
      }
      hasAdditionalDeps = true;
    }
    if (!hasAdditionalDeps) {
      return;
    }

    const buildPath = path.resolve(this.serverPath, 'app', 'build.gradle.kts');
    let configuration = await fs.readFile(buildPath, 'utf8');
    for (const propName of DEPENDENCY_PROP_NAMES) {
      const prefix = propName === DEPENDENCY_PROP_NAMES[0]
        ? 'implementation'
        : 'androidTestImplementation';
      const deps = this[propName]
        .filter((line) => _.trim(line))
        .map((line) => `${prefix}("${line}")`);
      if (_.isEmpty(deps)) {
        continue;
      }

      log.info(`Adding the following ${propName} to build.gradle.kts: ${deps}`);
      configuration = updateDependencyLines(configuration, propName, deps);
    }
    await fs.writeFile(buildPath, configuration, 'utf8');
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

export { ServerBuilder, VERSION_KEYS, GRADLE_URL_TEMPLATE, buildServerSigningConfig };
export default ServerBuilder;
