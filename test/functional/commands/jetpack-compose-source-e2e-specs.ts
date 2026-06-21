import {describe, it, before, after} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {DOMParser} from '@xmldom/xmldom';
import xpath from 'xpath';
import {initSession, deleteSession} from '../helpers/session.js';
import {type ComposeCaps, getComposeCaps} from '../desired.js';

use(chaiAsPromised);

describe('source commands', function () {

  let driver: any;
  let composeCaps: ComposeCaps;

  describe('jetpack-compose app', function () {
    before(async function (t) {
      // For SDK 23 and below Jetpack compose app crashes while running under instrumentation.
      if (parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 23) {
        return (t as any).skip();
      }
      composeCaps = await getComposeCaps();
      driver = await initSession(composeCaps);
    });
    after(async function () {
      await deleteSession();
    });

    it('should get jetpack-compose sourceXML, parse it, and find a node by xpath', async function () {
      const el = await driver.$("//*[@text='Display Text']");
      await el.click();
      await driver.updateSettings({driver: 'compose'});
      const sourceXML = await driver.getPageSource();
      expect(sourceXML).to.be.a.string;
      const doc = new DOMParser().parseFromString(sourceXML, 'application/xml');
      const node = xpath.select('//*', doc as unknown as Node);
      expect(node).to.exist;
    });
  });
});
