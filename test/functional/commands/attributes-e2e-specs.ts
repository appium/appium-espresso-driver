import {describe, it, before, after} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import type {Browser} from 'webdriverio';
import {initSession, deleteSession} from '../helpers/session.js';
import {APIDEMO_CAPS} from '../desired.js';

use(chaiAsPromised);

describe('element attributes', function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });
  describe('getAttribute', function () {
    it(`should get the 'content-desc' of a View`, async function () {
      const el = await driver.$("//*[@text='Animation']");
      await expect(el.getAttribute('content-desc')).to.eventually.equal('Animation');
    });
    it(`should get the 'text' of a View`, async function () {
      const el = await driver.$("//*[@text='Animation']");
      await expect(el.getAttribute('text')).to.eventually.equal('Animation');
    });
    it('should not work if getting an attribute that does not exist', async function () {
      const el = await driver.$("//*[@text='Animation']");
      await expect(el.getAttribute('some-fake-property')).to.be.rejectedWith(
        /Attribute name should be one of/,
      );
    });
  });
});
