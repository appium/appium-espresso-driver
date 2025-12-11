// @ts-nocheck
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

    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set value and replace them', async function () {
    await driver.$('~App').click();
    await driver.$('~Activity').click();
    await driver.$('~Custom Title').click();

    const el = await driver.$(await driver.findElement('class name', 'android.widget.EditText'));
    await driver.setValueImmediate(el.elementId, 'hello');
    await el.getText().should.eventually.equal('Left is besthello');
    await el.setValue('テスト');
    await el.getText().should.eventually.equal('テスト');
  });
});
