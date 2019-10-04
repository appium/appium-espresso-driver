import { fs, mkdirp } from 'appium-support';
import nodefs from 'fs';
import { copyGradleProjectRecursively } from '../../lib/utils';
import os from 'os';
import path from 'path';
import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';

chai.should();
chai.use(chaiAsPromised);
const expect = chai.expect;

describe('copyGradleProjectRecursively', function () {
  let baseSrcDir;
  let baseDestDir;

  function expectNotExist (dir) {
    expect(function () {
      fs.accessSync(dir, fs.constants.F_OK);
    }).to.throw();
  }

  async function expectCorrectFileContentIn (filepath) {
    await fs.readFile(filepath, 'utf8').should.eventually.eql('foobar');
  }

  async function createTestFile (filepath) {
    await fs.writeFile(filepath, 'foobar', 'utf8');
  }

  beforeEach(function () {
    baseSrcDir = nodefs.mkdtempSync(path.join(os.tmpdir(), 'appium-src-'));
    baseDestDir = nodefs.mkdtempSync(path.join(os.tmpdir(), 'appium-dst-'));
  });

  afterEach(async function () {
    await fs.rimraf(baseSrcDir);
    await fs.rimraf(baseDestDir);
  });

  it('copies all files not having "build" in their paths', async function () {
    await mkdirp(path.join(baseSrcDir, 'build'));
    await mkdirp(path.join(baseSrcDir, 'dir', 'build'));

    await createTestFile(path.join(baseSrcDir, 'build', 'file'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'build', 'file'));

    await copyGradleProjectRecursively(baseSrcDir, baseDestDir);

    await expectNotExist(path.join(baseSrcDir, 'build', 'file'));
    await expectNotExist(path.join(baseSrcDir, 'dir', 'build', 'file'));
  });

  it('doesn\'t copy any build directory', async function () {
    await mkdirp(path.join(baseSrcDir, 'foo'));
    await mkdirp(path.join(baseSrcDir, 'dir', 'foo'));

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
    await mkdirp(path.join(baseSrcDir, 'foo'));
    await mkdirp(path.join(baseSrcDir, 'dir', 'foo'));

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
