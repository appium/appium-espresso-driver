import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('ElementValue', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.app.CustomTitle',
    }, APIDEMO_CAPS);
    driver = await initSession(caps);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set value and replace them', async function () {
    let el = await driver.elementById('io.appium.android.apis:id/left_text_edit');
    await el.setImmediateValue(['hello']);

    let elValue = await driver.elementById('io.appium.android.apis:id/left_text_edit');
    await elValue.text().should.eventually.equal('Left is besthello');

    elValue.setText(['テスト']);
    await elValue.text().should.eventually.equal('テスト');
  });
});
