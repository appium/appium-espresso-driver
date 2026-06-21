import {describe, it, before, after} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import type {Browser} from 'webdriverio';
import {initSession, deleteSession} from '../helpers/session.js';
import {APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

describe('Size', function () {
  let driver: Browser;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should find rect of window', async function () {
    const {width, height, x, y} = await driver.getWindowRect();
    expect(width).to.be.above(0);
    expect(height).to.be.above(0);
    expect(x).to.equal(0);
    expect(y).to.equal(0);
  });

  it('should find rect of an element', async function () {
    const el = await driver.$('~App');
    const {width, height, x, y} = await driver.getElementRect(await el.elementId);
    expect(width).to.be.above(0);
    expect(height).to.be.above(0);
    // the element start from the edge of left.
    expect(x).to.equal(0);
    expect(y).to.be.above(0);
  });
});
