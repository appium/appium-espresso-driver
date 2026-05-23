import path from 'node:path';
import {fs, net, tempDir} from 'appium/support';

const COMPOSE_PLAYGROUND_URL =
  'https://github.com/appium/compose-playground/releases/download/v1.1.0/ComposePlayground-debug.apk';
const COMPOSE_PLAYGROUND_CACHE_PATH = path.resolve(
  __dirname,
  'fixtures',
  'ComposePlayground-debug.apk',
);

// Cache the download promises to prevent concurrent downloads
const downloadPromises = new Map<string, Promise<string>>();

/**
 * Downloads the Compose Playground APK from GitHub if it does not already exist locally.
 * Concurrent callers reuse the same download promise.
 */
export async function getComposePlaygroundPath(): Promise<string> {
  return downloadApp(
    COMPOSE_PLAYGROUND_URL,
    COMPOSE_PLAYGROUND_CACHE_PATH,
    'ComposePlayground-debug.apk',
  );
}

async function downloadApp(url: string, cachePath: string, fileName: string): Promise<string> {
  if (downloadPromises.has(cachePath)) {
    return downloadPromises.get(cachePath)!;
  }

  if (await fs.exists(cachePath)) {
    return cachePath;
  }

  const downloadPromise = (async (): Promise<string> => {
    try {
      if (await fs.exists(cachePath)) {
        return cachePath;
      }

      const fixturesDir = path.dirname(cachePath);
      await fs.mkdir(fixturesDir, {recursive: true});

      const tmpDir = await tempDir.openDir();
      const tmpPath = path.join(tmpDir, fileName);

      try {
        await net.downloadFile(url, tmpPath);
        await fs.mv(tmpPath, cachePath, {mkdirp: true});
      } finally {
        await fs.rimraf(tmpDir);
      }

      return cachePath;
    } finally {
      downloadPromises.delete(cachePath);
    }
  })();

  downloadPromises.set(cachePath, downloadPromise);
  return downloadPromise;
}
