import {describe, it, before, after} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import type {Browser} from 'webdriverio';
import {initSession, deleteSession} from '../helpers/session.js';
import {APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

describe('ElementValue', function () {

  let driver: Browser;

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
    await driver.setValueImmediate(await el.elementId, 'hello');
    await expect(el.getText()).to.eventually.equal('Left is besthello');
    await el.setValue('テスト');
    await expect(el.getText()).to.eventually.equal('テスト');
  });
});
