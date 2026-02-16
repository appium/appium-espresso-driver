import {SubProcess} from 'teen_process';
import {fs, system} from 'appium/support';
import _ from 'lodash';
import path from 'node:path';
import {EOL} from 'node:os';
import {updateDependencyLines} from './utils';
import type {AppiumLogger} from '@appium/types';

const GRADLE_VERSION_KEY = 'gradle';
const GRADLE_URL_PREFIX = 'distributionUrl=';
export const GRADLE_URL_TEMPLATE =
  'https\\://services.gradle.org/distributions/gradle-VERSION-all.zip';
const DEPENDENCY_PROP_NAMES = [
  'additionalAppDependencies',
  'additionalAndroidTestDependencies',
] as const;

export const VERSION_KEYS = [
  GRADLE_VERSION_KEY,
  'androidGradlePlugin',
  'compileSdk',
  'buildTools',
  'minSdk',
  'targetSdk',
  'kotlin',
  'sourceCompatibility',
  'targetCompatibility',
  'jvmTarget',
  'composeVersion',
  'espressoVersion',
  'annotationVersion',
] as const;

export interface ServerSigningConfig {
  zipAlign: boolean;
  keystoreFile: string;
  keystorePassword: string;
  keyAlias: string;
  keyPassword: string;
}

export interface BuildServerSigningConfigArgs {
  keystoreFile: string;
  keystorePassword: string;
  keyAlias: string;
  keyPassword: string;
}

export function buildServerSigningConfig(args: BuildServerSigningConfigArgs): ServerSigningConfig {
  return {
    zipAlign: true,
    keystoreFile: args.keystoreFile,
    keystorePassword: args.keystorePassword,
    keyAlias: args.keyAlias,
    keyPassword: args.keyPassword,
  };
}

interface BuildConfiguration {
  toolsVersions?: Record<string, string>;
  additionalAppDependencies?: string[];
  additionalAndroidTestDependencies?: string[];
}

export interface ServerBuilderOptions {
  serverPath: string;
  showGradleLog?: boolean;
  buildConfiguration?: BuildConfiguration;
  testAppPackage?: string;
  signingConfig?: ServerSigningConfig | null;
}

export class ServerBuilder {
  private readonly log: AppiumLogger;
  private readonly serverPath: string;
  private readonly showGradleLog?: boolean;
  private readonly serverVersions: Partial<Record<(typeof VERSION_KEYS)[number], string>>;
  private readonly testAppPackage?: string;
  private readonly signingConfig?: ServerSigningConfig | null;
  private readonly additionalAppDependencies: string[];
  private readonly additionalAndroidTestDependencies: string[];

  constructor(log: AppiumLogger, args: ServerBuilderOptions) {
    this.log = log;
    this.serverPath = args.serverPath;
    this.showGradleLog = args.showGradleLog;

    const buildConfiguration = args.buildConfiguration || {};

    const versionConfiguration = buildConfiguration.toolsVersions || {};
    this.serverVersions = _.reduce(
      versionConfiguration,
      (acc, value, key) => {
        if (VERSION_KEYS.includes(key as (typeof VERSION_KEYS)[number])) {
          acc[key as (typeof VERSION_KEYS)[number]] = value;
        } else {
          log.warn(`Got unexpected '${key}' in toolsVersion block of the build configuration`);
        }
        return acc;
      },
      {} as Partial<Record<(typeof VERSION_KEYS)[number], string>>,
    );

    this.testAppPackage = args.testAppPackage;
    this.signingConfig = args.signingConfig;

    for (const propName of DEPENDENCY_PROP_NAMES) {
      this[propName] = buildConfiguration[propName] || [];
    }
  }

  async build(): Promise<void> {
    const gradleVersion = this.serverVersions[GRADLE_VERSION_KEY];
    if (gradleVersion) {
      await this.setGradleWrapperVersion(gradleVersion);
    }

    await this.insertAdditionalDependencies();

    await this.runBuildProcess();
  }

  private getCommand(): {cmd: string; args: string[]} {
    const cmd = system.isWindows() ? 'gradlew.bat' : path.resolve(this.serverPath, 'gradlew');
    const buildProperty = (key: string, value?: string): string | null =>
      value ? `-P${key}=${value}` : null;
    const args: string[] = VERSION_KEYS.filter((key) => key !== GRADLE_VERSION_KEY)
      .map((key) => {
        const serverVersion = this.serverVersions[key];
        const gradleProperty = `appium${key.charAt(0).toUpperCase()}${key.slice(1)}`;
        return buildProperty(gradleProperty, serverVersion);
      })
      .filter((arg): arg is string => typeof arg === 'string' && Boolean(arg));

    const signingConfig = this.signingConfig;
    if (signingConfig) {
      args.push(
        ..._.keys(signingConfig)
          .map((key) => {
            const propKey = key as keyof ServerSigningConfig;
            const propValue = signingConfig[propKey];
            const k = `appium${_.upperFirst(key)}`;
            const v: string | undefined = !_.isNil(propValue) ? String(propValue) : undefined;
            return buildProperty(k, v);
          })
          .filter((arg): arg is string => typeof arg === 'string' && Boolean(arg)),
      );
    }

    if (this.testAppPackage) {
      const targetPackageArg = buildProperty('appiumTargetPackage', this.testAppPackage);
      if (targetPackageArg) {
        args.push(targetPackageArg);
      }
    }
    args.push('app:assembleAndroidTest');

    return {cmd, args};
  }

  private async setGradleWrapperVersion(version: string): Promise<void> {
    const propertiesPath = path.resolve(
      this.serverPath,
      'gradle',
      'wrapper',
      'gradle-wrapper.properties',
    );
    const originalProperties = await fs.readFile(propertiesPath, 'utf8');
    const newProperties = this.updateGradleDistUrl(originalProperties, version);
    await fs.writeFile(propertiesPath, newProperties, 'utf8');
  }

  private updateGradleDistUrl(propertiesContent: string, version: string): string {
    return propertiesContent.replace(
      new RegExp(`^(${_.escapeRegExp(GRADLE_URL_PREFIX)}).+$`, 'gm'),
      `$1${GRADLE_URL_TEMPLATE.replace('VERSION', version)}`,
    );
  }

  private async insertAdditionalDependencies(): Promise<void> {
    let hasAdditionalDeps = false;
    for (const propName of DEPENDENCY_PROP_NAMES) {
      const deps = this[propName];
      if (!_.isArray(deps)) {
        throw new Error(`'${propName}' must be an array`);
      }
      if (_.isEmpty(deps.filter((line) => _.trim(line)))) {
        continue;
      }

      for (const dep of deps) {
        if (/[\s'\\$]/.test(dep)) {
          throw new Error(
            'Single quotes, dollar characters and whitespace characters' +
              ` are disallowed in additional dependencies: ${dep}`,
          );
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
      const prefix = propName === DEPENDENCY_PROP_NAMES[0] ? 'api' : 'androidTestImplementation';
      const deps = this[propName]
        .filter((line) => _.trim(line))
        .map((line) => `${prefix}("${line}")`);
      if (_.isEmpty(deps)) {
        continue;
      }

      this.log.info(`Adding the following ${propName} to build.gradle.kts: ${deps}`);
      configuration = updateDependencyLines(configuration, propName, deps);
    }
    await fs.writeFile(buildPath, configuration, 'utf8');
  }

  private async runBuildProcess(): Promise<void> {
    const {cmd, args} = this.getCommand();
    this.log.debug(
      `Beginning build with command '${cmd} ${args.join(' ')}' ` +
        `in directory '${this.serverPath}'`,
    );
    const gradlebuild = new SubProcess(cmd, args, {
      cwd: this.serverPath,
      stdio: ['ignore', 'pipe', 'pipe'],
      // https://github.com/nodejs/node/issues/52572
      shell: system.isWindows(),
      windowsVerbatimArguments: true,
    });
    const gradleError: string[] = [];

    const logMsg = `Output from Gradle ${this.showGradleLog ? 'will' : 'will not'} be logged`;
    this.log.debug(`${logMsg}. To change this, use 'showGradleLog' desired capability`);
    gradlebuild.on('line-stderr', (line: string) => {
      this.log.warn(`[Gradle] ${line}`);
      gradleError.push(line);
    });
    gradlebuild.on('line-stdout', (line: string) => this.log.info(`[Gradle] ${line}`));

    try {
      await gradlebuild.start();
      await gradlebuild.join();
    } catch (err: any) {
      const msg =
        `Unable to build Espresso server - ${err.message}\n` +
        `Gradle error message:${EOL}${gradleError.join('\n')}`;
      throw this.log.errorWithException(msg);
    } finally {
      gradlebuild.removeAllListeners();
    }
  }
}
