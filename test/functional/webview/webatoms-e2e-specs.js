import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';
import B from 'bluebird';


describe('mobile web atoms', function () {
  this.timeout(MOCHA_TIMEOUT);
  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    driver = await initSession({
      ...APIDEMO_CAPS,
      appPackage: 'io.appium.android.apis',
      appActivity: 'io.appium.android.apis.view.WebView1',
    });
  });
  after(async function () {
    await deleteSession();
  });

  it('should input text into textbox and click links', async function () {
    const webviewEl = await driver.elementById('wv1');
    await B.delay(10000); // Wait for WebView to load
    await driver.execute(`mobile: webAtoms`, {
      webviewElement: webviewEl.value,
      forceJavascriptEnabled: true,
      methodChain: [
        {name: 'withElement', atom: {name: 'findElement', locator: {using: 'ID', value: 'i_am_a_textbox'}}},
        {name: 'perform', atom: {name: 'webKeys', args: 'Hello world'}},
        {name: 'withElement', atom: {name: 'findElement', locator: {using: 'ID', value: 'i am a link'}}},
        {name: 'perform', atom: 'webClick'},
      ]
    });
  });
});
