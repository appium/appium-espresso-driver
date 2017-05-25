import sampleApps from 'sample-apps';


const GENERIC_CAPS = {
  androidInstallTimeout: 90000,
  deviceName: 'Android',
  platformName: 'Android',
  forceEspressoRebuild: true,
};

const APIDEMO_CAPS = Object.assign({},
  GENERIC_CAPS,
  {
    app: sampleApps('ApiDemos-debug'),
  }
);

export { GENERIC_CAPS, APIDEMO_CAPS };
