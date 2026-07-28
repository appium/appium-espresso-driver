import releaseConfig from '@appium/semantic-release-config';

export default releaseConfig({
  extraGitAssets: ['espresso-server/library/src/main/java/io/appium/espressoserver/lib/helpers/Version.kt'],
});
