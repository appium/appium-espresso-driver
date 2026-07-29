import assert from 'node:assert/strict';
import {describe, it, before, beforeEach, afterEach} from 'node:test';

import {type ComposeCaps, getComposeCaps} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('Jetpack Compose', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;
  let composeCaps: ComposeCaps;

  before(async function () {
    composeCaps = await getComposeCaps();
  });

  beforeEach(async function () {
    driver = await initSession(composeCaps);
  });

  afterEach(async function () {
    await deleteSession();
  });

  it('should find element by tag and text and click it', async function () {
    const el = await driver.$("//*[@text='Clickable Component']");
    await el.click();

    await driver.updateSettings({driver: 'compose'});

    const e = await driver.$(await driver.findElement('tag name', 'lol'));
    assert.strictEqual(await driver.isElementDisplayed(e.elementId), true);

    const elementWithDescription = await driver.$('~desc');
    assert.strictEqual(await elementWithDescription.getText(), 'Click to see dialog');
    assert.strictEqual(await driver.isElementDisplayed(elementWithDescription.elementId), true);

    const clickableText = await driver.$('=Click to see dialog');
    await clickableText.click();

    await driver.$('=Congratulations! You just clicked the text successfully');
    assert.deepStrictEqual(await driver.getSettings(), {driver: 'compose'});
  });

  it('should find element by xpath', async function () {
    await driver.updateSettings({driver: 'espresso'});
    const el = await driver.$("//*[@text='Clickable Component']");
    await el.click();

    await driver.updateSettings({driver: 'compose'});

    const e = await driver.$("//*[@view-tag='lol']//*[@content-desc='desc']");
    assert.strictEqual(await e.getText(), 'Click to see dialog');
  });

  it('should find elements', async function () {
    await driver.updateSettings({driver: 'espresso'});
    const el = await driver.$("//*[@text='Horizontal Carousel']");
    await el.click();

    await driver.updateSettings({driver: 'compose'});

    const e = await driver.$$('=Grace Hopper');
    assert.deepStrictEqual(e.length, 2);
    assert.strictEqual(await e[0].getText(), 'Grace Hopper');
  });
});
