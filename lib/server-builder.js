import { SubProcess } from 'teen_process';
import { fs, logger } from 'appium-support';
import log from './logger';
import path from 'path';
import { EOL } from 'os';

const IGNORED_ERRORS = [];
const GRADLE_VERSION_KEY = 'gradle_version';
const GRADLE_URL_PREFIX = 'distributionUrl=';
const GRADLE_URL_TEMPLATE = 'https\\://services.gradle.org/distributions/gradle-VERSION-all.zip';
const GRADLE_MAX_ERROR_LOG_LINES = 15;

const VERSION_KEYS = [
  GRADLE_VERSION_KEY,
  'android_gradle_plugin_version',
  'compile_sdk_version',
  'build_tools_version',
  'min_sdk_version',
  'target_sdk_version',
  'kotlin_version'
];

const gradleLog = logger.getLogger('Gradle');

class ServerBuilder {
  constructor (args = {}) {
    this.serverPath = args.serverPath;
    this.showGradleLog = args.showGradleLog;

    const versionsArg = args.versions || {};
    const serverVersions = {};
    for (const key of VERSION_KEYS) {
      if (versionsArg[key]) {
        serverVersions[key] = versionsArg[key];
      }
    }
    this.server_versions = serverVersions;
  }

  async build () {
    if (this.server_versions[GRADLE_VERSION_KEY]) {
      await this.setGradleWrapperVersion(this.server_versions[GRADLE_VERSION_KEY]);
    }
    await this.runBuildProcess();
  }

  getCommand () {
    let cmd = `.${path.sep}gradlew`;
    let args = VERSION_KEYS
      .filter(key => key !== GRADLE_VERSION_KEY)
      .map(key => {
        const serverVersion = this.server_versions[key];
        return serverVersion ? `-P${key}=${serverVersion}` : null;
      })
      .filter(serverVersion => serverVersion);

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
          return GRADLE_URL_PREFIX + GRADLE_URL_TEMPLATE.replace('VERSION', version);
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

    const logGradleOutput = this.showGradleLog;
    const logMsg = `Output from Gradle ${this.showGradleLog ? 'will' : 'will not'} be logged`;
    log.debug(`${logMsg}. To change this, use 'showGradleLog' desired capability`);
    gradlebuild.on('stream-line', line => {
      // if we have an error we want to output the logs
      // otherwise the failure is inscrutible
      // but do not log expected errors
      const ignoreError = IGNORED_ERRORS.some((x) => line.includes(x));

      if (logGradleOutput && !ignoreError) {
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
      log.error(msg);
      throw new Error(msg);
    }
  }
}

export { ServerBuilder, VERSION_KEYS, GRADLE_URL_TEMPLATE };
export default ServerBuilder;