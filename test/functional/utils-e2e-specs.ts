import {describe, it, before, beforeEach, afterEach} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import path from 'node:path';
import {fs, tempDir} from 'appium/support.js';
import {copyGradleProjectRecursively} from '../../lib/commands/server/runner.js';

use(chaiAsPromised);

describe('copyGradleProjectRecursively', function () {
  let baseSrcDir: string;
  let baseDestDir: string;

  async function expectNotExist(file: string) {
    await expect(fs.access(file, fs.constants.F_OK)).to.be.rejectedWith(/no such file/);
  }

  async function expectCorrectFileContentIn(filepath: string) {
    await expect(fs.readFile(filepath, 'utf8')).to.eventually.eql('foobar');
  }

  async function createTestFile(filepath: string) {
    await fs.writeFile(filepath, 'foobar', 'utf8');
  }

  before(async function () {
    // chai initialized
  });

  beforeEach(async function () {
    baseSrcDir = await tempDir.openDir();
    baseDestDir = await tempDir.openDir();
  });

  afterEach(async function () {
    await fs.rimraf(baseSrcDir);
    await fs.rimraf(baseDestDir);
  });

  it("doesn't copy any build directory", async function () {
    await fs.mkdirp(path.join(baseSrcDir, 'build'));
    await fs.mkdirp(path.join(baseSrcDir, 'dir', 'build'));

    await createTestFile(path.join(baseSrcDir, 'build', 'file'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'build', 'file'));

    await copyGradleProjectRecursively(baseSrcDir, baseDestDir);

    await expectNotExist(path.join(baseDestDir, 'build', 'file'));
    await expectNotExist(path.join(baseDestDir, 'dir', 'build', 'file'));
  });

  it('copies all files not having "build" in their paths', async function () {
    await fs.mkdirp(path.join(baseSrcDir, 'foo'));
    await fs.mkdirp(path.join(baseSrcDir, 'dir', 'foo'));

    await createTestFile(path.join(baseSrcDir, 'file'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'file'));
    await createTestFile(path.join(baseSrcDir, 'foo', 'file'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'foo', 'file'));

    await copyGradleProjectRecursively(baseSrcDir, baseDestDir);

    await expectCorrectFileContentIn(path.join(baseDestDir, 'file'));
    await expectCorrectFileContentIn(path.join(baseDestDir, 'dir', 'file'));
    await expectCorrectFileContentIn(path.join(baseDestDir, 'foo', 'file'));
    await expectCorrectFileContentIn(path.join(baseDestDir, 'dir', 'foo', 'file'));
  });

  it('copies files named "build"', async function () {
    await fs.mkdirp(path.join(baseSrcDir, 'foo'));
    await fs.mkdirp(path.join(baseSrcDir, 'dir', 'foo'));

    await createTestFile(path.join(baseSrcDir, 'build'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'build'));
    await createTestFile(path.join(baseSrcDir, 'foo', 'build'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'foo', 'build'));

    await copyGradleProjectRecursively(baseSrcDir, baseDestDir);

    await expectCorrectFileContentIn(path.join(baseDestDir, 'build'));
    await expectCorrectFileContentIn(path.join(baseDestDir, 'dir', 'build'));
    await expectCorrectFileContentIn(path.join(baseDestDir, 'foo', 'build'));
    await expectCorrectFileContentIn(path.join(baseDestDir, 'dir', 'foo', 'build'));
  });
});
