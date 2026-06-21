import {describe, it, before, beforeEach, afterEach} from 'node:test';
import {expect, use} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';
import {type ComposeCaps, getComposeCaps} from '../desired.js';

use(chaiAsPromised);

const SKIP_COMPOSE_TESTS = parseInt(process.env.ANDROID_SDK_VERSION ?? '0', 10) <= 23;
describe(
  'compose node attributes',
  {skip: SKIP_COMPOSE_TESTS, timeout: E2E_TEST_TIMEOUT},
  function () {
    let driver: any;
    let composeCaps: ComposeCaps;

    before(async function () {
      composeCaps = await getComposeCaps();
    });

    describe('compose getAttribute', function () {
      beforeEach(async function () {
        driver = await initSession(composeCaps);
      });

      afterEach(async function () {
        await deleteSession();
      });

      it(`should get attributes of a View`, async function () {
        const el = await driver.$("//*[@text='Clickable Component']");
        await el.click();

        await driver.updateSettings({driver: 'compose'});

        const taggedElement = await driver.$('<lol>');
        await expect(taggedElement.getAttribute('view-tag')).to.eventually.equal('lol');

        const click_dialog = await driver.$("//*[@text='Click to see dialog']");
        await expect(click_dialog.getAttribute('text')).to.eventually.equal('Click to see dialog');
        await expect(click_dialog.getText()).to.eventually.equal('Click to see dialog');

        await expect(click_dialog.getAttribute('selected')).to.eventually.equal('false');
        await expect(click_dialog.isSelected()).to.eventually.be.false;

        await expect(click_dialog.isDisplayed()).to.eventually.be.true;

        await expect(click_dialog.getAttribute('class')).to.eventually.equal('Text');

        await expect(click_dialog.getAttribute('clickable')).to.eventually.equal('false');

        await expect(click_dialog.getAttribute('enabled')).to.eventually.equal('true');
        await expect(click_dialog.isEnabled()).to.eventually.be.true;

        await expect(click_dialog.getAttribute('focused')).to.eventually.equal('false');
      });
    });
  },
);
