import assert from 'node:assert/strict';
import {describe, it, before, after, beforeEach} from 'node:test';

import axios from 'axios';

import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, HOST, PORT, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('keyboard', {timeout: E2E_TEST_TIMEOUT}, function () {
  let idCounter = 0;

  const performActions = async function (...actionsArrays: any[]) {
    const actionsRoot: any[] = [];

    for (const actions of actionsArrays) {
      actionsRoot.push({
        type: 'key',
        id: `id_${idCounter++}`,
        actions,
      });
    }

    const sessionId = await driver.sessionId;
    return (
      await axios({
        method: 'POST',
        url: `http://${HOST}:${PORT}/session/${sessionId}/actions`,
        data: {actions: actionsRoot},
      })
    ).data;
  };

  let driver: any;

  before(async function () {
    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:autoGrantPermissions': true,
        'appium:appActivity': 'io.appium.android.apis.view.AutoComplete4',
      }),
    );
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
    assert.strictEqual(await el.getText(), 'hello world');
    await driver.setValueImmediate(el.elementId, '!!!');
    assert.strictEqual(await el.getText(), 'hello world!!!');
    await driver.elementClear(el.elementId);
  });

  it('should perform key events', async function () {
    const autocompleteEl = await driver.$('//android.widget.AutoCompleteTextView');
    await autocompleteEl.click();
    const keyActions = [
      {type: 'keyDown', value: '\uE008'},
      {type: 'keyDown', value: 'h'},
      {type: 'keyUp', value: 'h'},
      {type: 'keyDown', value: 'a'},
      {type: 'keyUp', value: 'a'},
      {type: 'pause', duration: 2000},
      {type: 'keyUp', value: '\uE008'},
      {type: 'keyDown', value: 't'},
      {type: 'keyUp', value: 't'},
      {type: 'keyDown', value: 'S'},
      {type: 'keyUp', value: 'S'},
    ];
    await performActions(keyActions);
    assert.strictEqual(await autocompleteEl.getText(), 'HAtS');
    await driver.elementClear(autocompleteEl.elementId);
  });
  describe('non-ASCII characters', function () {
    const ARROW_LEFT = '\uE012';
    const SHIFT = '\uE008';
    const CONTROL = '\uE009';
    const keyDown = (value: string) => ({type: 'keyDown', value});
    const keyUp = (value: string) => ({type: 'keyUp', value});
    const typeEach = (text: string) => Array.from(text).flatMap((ch) => [keyDown(ch), keyUp(ch)]);

    let el: any;

    beforeEach(async function () {
      el = await driver.$('//android.widget.AutoCompleteTextView');
      await el.click();
      await driver.elementClear(el.elementId);
      await el.click();
    });

    it('should type non-ASCII characters', async function () {
      await performActions(typeEach('Привіт'));
      assert.strictEqual(await el.getText(), 'Привіт');
    });

    it('should type characters from different scripts', async function () {
      await performActions(typeEach('日本語'));
      assert.strictEqual(await el.getText(), '日本語');
    });

    it('should type mixed ASCII and non-ASCII characters with per-key down/up', async function () {
      await performActions(typeEach('aжb'));
      assert.strictEqual(await el.getText(), 'aжb');
    });

    it('should type all keys down first and then all keys up', async function () {
      const chars = Array.from('aжbі');
      await performActions([...chars.map(keyDown), ...chars.map(keyUp)]);
      assert.strictEqual(await el.getText(), 'aжbі');
    });

    it('should type via the legacy keys endpoint', async function () {
      await axios.post(`http://${HOST}:${PORT}/session/${await driver.sessionId}/keys`, {
        value: Array.from('Привіт'),
      });
      assert.strictEqual(await el.getText(), 'Привіт');
    });

    it('should insert at the cursor position', async function () {
      await performActions(typeEach('тест'));
      await performActions([keyDown(ARROW_LEFT), keyUp(ARROW_LEFT)]);
      await performActions(typeEach('Ж'));
      assert.strictEqual(await el.getText(), 'тесЖт');
    });

    it('should replace the selection', async function () {
      await performActions(typeEach('тест'));
      await performActions([keyDown(CONTROL), keyDown('a'), keyUp('a'), keyUp(CONTROL)]);
      await performActions(typeEach('Ж'));
      assert.strictEqual(await el.getText(), 'Ж');
    });

    it('should type surrogate pairs and combining sequences', async function () {
      await performActions(typeEach('\u{1F600}री'));
      assert.strictEqual(await el.getText(), '\u{1F600}री');
    });

    it('should fail for an unknown special key', async function () {
      await assert.rejects(performActions([keyDown('\uE0FF'), keyUp('\uE0FF')]), (err: any) => {
        assert.strictEqual(err.response?.status, 400);
        return true;
      });
      assert.strictEqual(await el.getText(), '');
    });

    it('should fail if a modifier is pressed', async function () {
      const send = async (actions: any[]) =>
        await axios.post(`http://${HOST}:${PORT}/session/${await driver.sessionId}/actions`, {
          actions: [{type: 'key', id: 'kbd-modifier', actions}],
        });
      await assert.rejects(send([keyDown(SHIFT), keyDown('ж'), keyUp('ж'), keyUp(SHIFT)]), (err: any) => {
        assert.strictEqual(err.response?.status, 400);
        return true;
      });
      // The failed request leaves Shift pressed in the session input state of the same source
      await send([keyUp(SHIFT)]);
    });
  });
});

describe('keyboard non-ASCII in a password field', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;

  before(async function () {
    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:autoGrantPermissions': true,
        'appium:appActivity': 'io.appium.android.apis.view.TextFields',
      }),
    );
  });
  after(async function () {
    await deleteSession();
  });

  it('should type into a password field', async function () {
    const pwd = await driver.$('id=io.appium.android.apis:id/edit1');
    await pwd.click();
    const actions = Array.from('пароль').flatMap((value) => [
      {type: 'keyDown', value},
      {type: 'keyUp', value},
    ]);
    await axios.post(`http://${HOST}:${PORT}/session/${await driver.sessionId}/actions`, {
      actions: [{type: 'key', id: 'kbd', actions}],
    });
    assert.strictEqual(await pwd.getText(), 'пароль');
  });
});

describe('keyboard non-ASCII in a dialog', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;

  before(async function () {
    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        'appium:autoGrantPermissions': true,
        'appium:appActivity': 'io.appium.android.apis.app.AlertDialogSamples',
      }),
    );
  });
  after(async function () {
    await deleteSession();
  });

  it('should type into a text field of a dialog', async function () {
    await (await driver.$('id=io.appium.android.apis:id/text_entry_button')).click();
    const field = await driver.$('id=io.appium.android.apis:id/username_edit');
    await field.click();
    const actions = Array.from('Привіт').flatMap((value) => [
      {type: 'keyDown', value},
      {type: 'keyUp', value},
    ]);
    await axios.post(`http://${HOST}:${PORT}/session/${await driver.sessionId}/actions`, {
      actions: [{type: 'key', id: 'kbd', actions}],
    });
    assert.strictEqual(await field.getText(), 'Привіт');
  });
});
