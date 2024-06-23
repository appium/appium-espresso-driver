import { fs, mkdirp, tempDir } from 'appium/support';
import { copyGradleProjectRecursively } from '../../lib/utils';
import path from 'path';


describe('copyGradleProjectRecursively', function () {
  let baseSrcDir;
  let baseDestDir;
  let chai;

  async function expectNotExist (file) {
    await fs.access(file, fs.constants.F_OK).should.eventually.be.rejectedWith(/no such file/);
  }

  async function expectCorrectFileContentIn (filepath) {
    await fs.readFile(filepath, 'utf8').should.eventually.eql('foobar');
  }

  async function createTestFile (filepath) {
    await fs.writeFile(filepath, 'foobar', 'utf8');
  }

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
  });

  beforeEach(async function () {
    baseSrcDir = await tempDir.openDir();
    baseDestDir = await tempDir.openDir();
  });

  afterEach(async function () {
    await fs.rimraf(baseSrcDir);
    await fs.rimraf(baseDestDir);
  });

  it('doesn\'t copy any build directory', async function () {
    await mkdirp(path.join(baseSrcDir, 'build'));
    await mkdirp(path.join(baseSrcDir, 'dir', 'build'));

    await createTestFile(path.join(baseSrcDir, 'build', 'file'));
    await createTestFile(path.join(baseSrcDir, 'dir', 'build', 'file'));

    await copyGradleProjectRecursively(baseSrcDir, baseDestDir);

    await expectNotExist(path.join(baseDestDir, 'build', 'file'));
    await expectNotExist(path.join(baseDestDir, 'dir', 'build', 'file'));
  });

  it('copies all files not having "build" in their paths', async function () {
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
