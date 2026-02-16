import {doctor as doctorCommon} from 'appium-android-driver';
import {exec} from 'teen_process';
import {fs, system, doctor} from 'appium/support';
import path from 'node:path';
import type {AppiumLogger, IDoctorCheck} from '@appium/types';

export const androidHomeCheck = doctorCommon.androidHomeCheck;
export const javaHomeCheck = doctorCommon.javaHomeCheck;
export const javaHomeValueCheck = doctorCommon.javaHomeValueCheck;
export const androidSdkCheck = doctorCommon.androidSdkCheck;

export class JavaVersionCheck implements IDoctorCheck {
  log!: AppiumLogger;
  MIN_JAVA_VERSION = 11;
  JAVA_VERSION_PATTERN = /^\s*java\.version\s*=\s*([\d.]+)/m;

  async diagnose() {
    const javaHome = process.env.JAVA_HOME;
    const fullJavaPath = path.join(
      javaHome ?? '',
      'bin',
      `java${system.isWindows() ? '.exe' : ''}`,
    );
    if (!javaHome || !(await fs.exists(fullJavaPath))) {
      return doctor.nok(
        `Cannot retrieve Java version. Is Java installed and JAVA_HOME set to a proper path?`,
      );
    }
    let javaVerMatch: RegExpExecArray | null = null;
    try {
      const {stderr} = await exec(fullJavaPath, ['-XshowSettings:properties', '-version']);
      javaVerMatch = this.JAVA_VERSION_PATTERN.exec(stderr);
    } catch (e: any) {
      return doctor.nok(`Cannot retrieve Java version: ${e.stderr || e.message}`);
    }
    if (!javaVerMatch) {
      return doctor.nok(`The actual Java version cannot be retrieved`);
    }
    const majorVer = parseInt(javaVerMatch[1], 10);
    return majorVer < this.MIN_JAVA_VERSION
      ? doctor.nok(`The active Java version ${javaVerMatch[1]} is older than the required one`)
      : doctor.ok(`The active Java version matches (${majorVer} >= ${this.MIN_JAVA_VERSION})`);
  }

  async fix() {
    return (
      `Upgrade to Java ${this.MIN_JAVA_VERSION}+ or update ` +
      `the JAVA_HOME environment variable if it is already installed`
    );
  }

  hasAutofix() {
    return false;
  }

  isOptional() {
    return false;
  }
}
export const javaVersionCheck = new JavaVersionCheck();
