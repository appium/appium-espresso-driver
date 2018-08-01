import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('keyboard', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.view.AutoComplete4'
    }, APIDEMO_CAPS);
    driver = await initSession(caps);
  });
  after(async function () {
    await deleteSession();
  });

  it('should send keys to the correct element', async function () {
    let el = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
    await el.click();

    await el.sendKeys('hello');
  });

  it.only('should send keys to the correct element', async function () {
    let el = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
    await el.setImmediateValue('hello world');
    await el.text().should.eventually.equal('hello world');
    await el.setImmediateValue('!!!');
    await el.text().should.eventually.equal('hello world!!!');
  });
});
