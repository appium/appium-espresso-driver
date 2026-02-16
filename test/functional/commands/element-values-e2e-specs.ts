import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('ElementValue', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;

  before(async function () {
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
    await expect(el.getText()).to.eventually.equal('Left is besthello');
    await el.setValue('テスト');
    await expect(el.getText()).to.eventually.equal('テスト');
  });
});
