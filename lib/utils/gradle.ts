import {fs} from 'appium/support';
import path from 'node:path';

/**
 * Recursively copy all files except build directories contents
 * @param sourceBaseDir directory to copy files from
 * @param targetBaseDir directory to copy files to
 * @returns Nothing
 */
export async function copyGradleProjectRecursively(
  sourceBaseDir: string,
  targetBaseDir: string,
): Promise<void> {
  // @ts-ignore it is ok to have the async callback
  await fs.walkDir(sourceBaseDir, true, async (itemPath: string, isDirectory: boolean) => {
    const relativePath = path.relative(sourceBaseDir, itemPath);
    const targetPath = path.resolve(targetBaseDir, relativePath);

    const isInGradleBuildDir = `${path.sep}${itemPath}`.includes(`${path.sep}build${path.sep}`);
    if (isInGradleBuildDir) {
      return false;
    }

    if (isDirectory) {
      await fs.mkdirp(targetPath);
    } else {
      await fs.copyFile(itemPath, targetPath);
    }
    return false;
  });
}
