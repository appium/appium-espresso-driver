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

    driver = await initSession({
      appActivity: 'io.appium.android.apis.app.CustomTitle',
      ...APIDEMO_CAPS
    });
  });
  after(async function () {
    await deleteSession();
  });

  it('should set value and replace them', async function () {
    let el = await driver.$('#io.appium.android.apis:id/left_text_edit');
    await driver.setValueImmediate(el.elementId, 'hello');

    let elValue = await driver.$('#io.appium.android.apis:id/left_text_edit');
    await elValue.getText().should.eventually.equal('Left is besthello');

    elValue.addValue('テスト');
    await elValue.getText().should.eventually.equal('テスト');
  });
});
