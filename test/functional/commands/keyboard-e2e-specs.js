import axios from 'axios';
import { initSession, deleteSession, MOCHA_TIMEOUT, HOST, PORT } from '../helpers/session';
import { amendCapabilities, APIDEMO_CAPS } from '../desired';


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

    const sessionId = await driver.sessionId;
    return (await axios({
      method: 'POST',
      url: `http://${HOST}:${PORT}/session/${sessionId}/actions`,
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

    driver = await initSession(amendCapabilities(
      APIDEMO_CAPS,
      {
        'appium:autoGrantPermissions': true,
        'appium:appActivity': 'io.appium.android.apis.view.AutoComplete4'
      }
    ));
  });
  after(async function () {
    await deleteSession();
  });

  it('should send keys to the correct element', async function () {
    const el = await driver.$('//android.widget.AutoCompleteTextView');
    await el.click();
    await driver.elementSendKeys(el.elementId, 'hello');
    await driver.elementClear(el.elementId);
  });

  it('should send keys to the correct element as replace text', async function () {
    const el = await driver.$('//android.widget.AutoCompleteTextView');
    await el.click();
    await driver.elementSendKeys(el.elementId, 'ハロー');
    await driver.elementClear(el.elementId);
  });

  it('should send keys to the correct element with setImmediateValue', async function () {
    const el = await driver.$('//android.widget.AutoCompleteTextView');
    await driver.setValueImmediate(el.elementId, 'hello world');
    await el.getText().should.eventually.equal('hello world');
    await driver.setValueImmediate(el.elementId, '!!!');
    await el.getText().should.eventually.equal('hello world!!!');
    await driver.elementClear(el.elementId);
  });

  it('should perform key events', async function () {
    let autocompleteEl = await driver.$('//android.widget.AutoCompleteTextView');
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
    await autocompleteEl.getText().should.eventually.equal('HAtS');
    await driver.elementClear(autocompleteEl.elementId);
  });
});
