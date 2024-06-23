import axios from 'axios';
import { initSession, deleteSession, MOCHA_TIMEOUT, HOST, PORT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('keyboard', function () {
  this.timeout(MOCHA_TIMEOUT);

  let idCounter = 0;

  const performActions = async function (...actionsArrays) {
    const actionsRoot = [];

    for (let actions of actionsArrays) {
      actionsRoot.push({
        type: 'key',
        id: `id_${idCounter++}`,
        actions,
      });
    }

    const sessionId = await driver.getSessionId();
    return (await axios({
      method: 'POST',
      url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/actions`,
      data: {actions: actionsRoot},
    })).data;
  };

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    let caps = Object.assign({
      appActivity: 'io.appium.android.apis.view.AutoComplete4',
      autoGrantPermissions: true,
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
    await el.clear();
  });

  it('should send keys to the correct element as replace text', async function () {
    let el = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
    await el.click();
    await el.sendKeys('ハロー');
    await el.clear();
  });

  it('should send keys to the correct element', async function () {
    let el = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
    await el.setImmediateValue(['hello world']);
    await el.text().should.eventually.equal('hello world');
    await el.setImmediateValue(['!!!']);
    await el.text().should.eventually.equal('hello world!!!');
    await el.clear();
  });

  it('should perform key events', async function () {
    let autocompleteEl = await driver.elementByXPath('//android.widget.AutoCompleteTextView');
    await autocompleteEl.click();
    const keyActions = [
      {'type': 'keyDown', 'value': '\uE008'},
      {'type': 'keyDown', 'value': 'h'},
      {'type': 'keyUp', 'value': 'h'},
      {'type': 'keyDown', 'value': 'a'},
      {'type': 'keyUp', 'value': 'a'},
      {'type': 'pause', 'duration': 2000},
      {'type': 'keyUp', 'value': '\uE008'},
      {'type': 'keyDown', 'value': 't'},
      {'type': 'keyUp', 'value': 't'},
      {'type': 'keyDown', 'value': 'S'},
      {'type': 'keyUp', 'value': 'S'},
    ];
    await performActions(keyActions);
    await autocompleteEl.text().should.eventually.equal('HAtS');
    await autocompleteEl.clear();
  });
});
