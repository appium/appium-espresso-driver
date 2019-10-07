import { SubProcess } from 'teen_process';
import { fs, logger, system } from 'appium-support';
import log from './logger';
import path from 'path';
import { EOL } from 'os';

const IGNORED_ERRORS = [];
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

class ServerBuilder {
  constructor (args = {}) {
    this.serverPath = args.serverPath;
    this.showGradleLog = args.showGradleLog;

    const buildConfiguration = args.buildConfiguration || {};

    const versionConfiguration = buildConfiguration.toolsVersions || {};
    const serverVersions = {};
    for (const key of VERSION_KEYS) {
      if (versionConfiguration[key]) {
        serverVersions[key] = versionConfiguration[key];
      }
    }
    this.serverVersions = serverVersions;

    this.additionalAppDependencies = buildConfiguration.additionalAppDependencies || [];
  }

  async build () {
    if (this.serverVersions[GRADLE_VERSION_KEY]) {
      await this.setGradleWrapperVersion(this.serverVersions[GRADLE_VERSION_KEY]);
    }
    await this.insertAdditionalAppDependencies(this.additionalAppDependencies);
    await this.runBuildProcess();
  }

  getCommand () {
    const cmd = system.isWindows() ? 'gradlew.bat' : './gradlew';
    let args = VERSION_KEYS
      .filter(key => key !== GRADLE_VERSION_KEY)
      .map(key => {
        const serverVersion = this.serverVersions[key];
        const gradleProperty = `appium${key.charAt(0).toUpperCase()}${key.slice(1)}`;
        return serverVersion ? `-P${gradleProperty}=${serverVersion}` : null;
      })
      .filter(Boolean);

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
    return propertiesContent.split('\n')
      .map(function (line) {
        if (line.startsWith(GRADLE_URL_PREFIX)) {
          return `${GRADLE_URL_PREFIX}${GRADLE_URL_TEMPLATE.replace('VERSION', version)}`;
        } else {
          return line;
        }
      })
      .join('\n');
  }

  async insertAdditionalAppDependencies (additionalDependencies) {
    const buildPath = path.resolve(this.serverPath, 'app', 'build.gradle');
    const originalConfiguration = await fs.readFile(buildPath, 'utf8');
    const newConfiguration = this.updateDependencyLines(originalConfiguration, additionalDependencies);
    await fs.writeFile(buildPath, newConfiguration, 'utf8');
  }

  updateDependencyLines (configurationContent, additionalDependencies) {
    const dependencyLines = additionalDependencies
      .map(function (dependency) {
        if (/[\s']/.test(dependency)) {
          throw new Error(`Disallowed characters in an additional dependency: ${dependency}`);
        }
        return `implementation '${dependency}'`;
      })
      .join('\n');

    return configurationContent.split('\n')
      .map(function (line) {
        if (line.trim() === GRADLE_DEPENDENCIES_PLACEHOLDER) {
          return dependencyLines;
        } else {
          return line;
        }
      })
      .join('\n');
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
    gradlebuild.on('stream-line', line => {
      // if we have an error we want to output the logs
      // otherwise the failure is inscrutible
      // but do not log expected errors
      const ignoreError = IGNORED_ERRORS.some((x) => line.includes(x));

      if (this.showGradleLog && !ignoreError) {
        gradleLog.error(line);
      }
      buildLastLines.push(`${EOL}${line}`);
      if (buildLastLines.length > GRADLE_MAX_ERROR_LOG_LINES) {
        buildLastLines = buildLastLines.slice(-GRADLE_MAX_ERROR_LOG_LINES);
      }
    });

    buildLastLines = [];

    try {
      await gradlebuild.start();
      await gradlebuild.join();
    } catch (err) {
      let msg = `Unable to build Espresso server - ${err}\n` +
        `Gradle error message:${EOL}${buildLastLines}`;
      log.errorAndThrow(msg);
    }
  }
}

export { ServerBuilder, VERSION_KEYS, GRADLE_URL_TEMPLATE, GRADLE_DEPENDENCIES_PLACEHOLDER };
export default ServerBuilder;