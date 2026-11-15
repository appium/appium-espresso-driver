import assert from 'node:assert/strict';
import {describe, it, before, after} from 'node:test';

import {APIDEMO_CAPS} from '../desired.js';
import {scrollListToEnd} from '../helpers/device.js';
import {isApiBelow} from '../helpers/env.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('find elements', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: any;

  describe('element by xpath', function () {
    describe('element by xpath - Dependent Test - Set 1', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
      });

      after(async function () {
        await deleteSession();
      });

      it(`should find an element by it's xpath`, async function () {
        const el = await driver.$("//*[@text='Animation']");
        assert.ok(el);
        await el.click();
        await driver.back();
      });
      it('should find an element by an XPath 2.0 expression', async function () {
        const els = await driver.$$("//*[matches(@text, '^Anim.*')]");
        assert.strictEqual(els.length, 1);
        assert.strictEqual(await els[0].getText(), 'Animation');
      });
      it('should report an invalid XPath expression as an invalid selector', async function () {
        await assert.rejects(driver.findElement('xpath', '//*[@text + 1 = 2]'), /invalid selector/i);
      });
      it('should find multiple elements that match one xpath', async function () {
        const els = await driver.$$('//android.widget.TextView');
        assert.ok(els.length > 1);
        await els[0].click();
        await driver.back();
      });
      it('should get the first element of an xpath that matches more than one element', async function () {
        const el = await driver.$('//android.widget.TextView');
        assert.ok(el);
        await el.click();
        await driver.back();
      });
      it('should throw a stale element exception if clicking on element that does not exist', async function () {
        const el = await driver.$("//*[@content-desc='Animation']");
        await el.click();
        try {
          await driver.elementClick(await el.elementId);
          throw Error('Should raise an error before this line.');
        } catch (err: unknown) {
          assert.strictEqual(err instanceof Error ? err.name : '', 'stale element reference');
        }
        await driver.back();
      });
      it('should get the isElementDisplayed attribute on the same element twice', async function () {
        const el = await driver.$("//*[@content-desc='Animation']");
        assert.strictEqual(await driver.isElementDisplayed(await el.elementId), true);
        await el.click();
        await driver.back();
      });
    });

    describe('ElementsByXpath - Dependent Test - Set 2', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
      });

      after(async function () {
        await deleteSession();
      });

      // The clipped list items cannot be clicked on the small API 26 emulator screen, even after scrolling
      const skipOffScreen = isApiBelow(29) && 'the API 26 emulator screen is too small';
      it(
        'should match an element if the element is off-screen but has an accessibility id',
        {skip: skipOffScreen},
        async function () {
          await scrollListToEnd(driver);
          const el = await driver.$('~Views');
          await el.click();

          // The element at the bottom of the list is found even if it is off-screen,
          // but it must be scrolled into view to be clicked
          const moveToEl = await driver.$('~WebView');
          assert.ok(await moveToEl.isExisting());
          await scrollListToEnd(driver);
          await driver.elementClick(await moveToEl.elementId);
          await driver.back();
          await driver.back();
        },
      );
    });
  });
  describe('by data matcher', function () {
    describe('Data Matcher - dependent tests - Set 1', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
      });

      after(async function () {
        await deleteSession();
      });
      it('should fail to find elements with helpful error messages', async function () {
        const err = (await driver.findElement(
          '-android datamatcher',
          JSON.stringify({
            name: 'hasEntry',
            args: ['title', 'A Fake Item'],
          }),
        )) as {error: string};
        assert.strictEqual(err.error, 'no such element');
      });
      it('should fail with invalid selector with helpful error messages', async function () {
        await assert.rejects(
          driver.findElement(
            '-android datamatcher',
            JSON.stringify({
              name: 'notARealHamcrestMatcherStrategy',
              args: ['title', 'A Fake Item'],
            }),
          ),
          /Not a valid selector/,
        );
      });
      it('should allow "class" property with fully qualified className', async function () {
        await assert.rejects(
          driver.findElement(
            '-android datamatcher',
            JSON.stringify({
              name: 'notARealHamcrestMatcherStrategy',
              args: ['title', 'A Fake Item'],
              class: 'org.hamcrest.Matchers',
            }),
          ),
          /Not a valid selector/,
        );
      });
      it('should find an element using a data matcher', async function () {
        const el = await driver.$(
          await driver.findElement(
            '-android datamatcher',
            JSON.stringify({
              name: 'hasEntry',
              args: ['title', 'Animation'],
            }),
          ),
        );
        await el.click();
        await driver.$('~Bouncing Balls');
        await driver.back();
      });
      it('should find an offscreen element using a data matcher', async function () {
        const viewsEl = await driver.$('~Views');
        await viewsEl.click();
        const el = await driver.$(
          await driver.findElement(
            '-android datamatcher',
            JSON.stringify({
              name: 'hasEntry',
              args: ['title', 'WebView3'],
            }),
          ),
        );
        await el.click();
        await driver.back();
        await driver.$('~Controls');
        await driver.back();
      });
    });
    describe('Data Matcher - dependent tests - Set 2', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.execute('mobile:startActivity', {
          appPackage: 'io.appium.android.apis',
          appActivity: '.view.SplitTouchView',
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should be able to set a specific AdapterView as a root element when activity has multiple AdapterViews', async function () {
        // Finding by adapter equalTo 'Zamorano' should be ambiguous, because there are two
        // adapter items with the same matcher

        // TODO: maybe need to update the test since this didn't occur.
        // This error comes from Espresso framework itself, so possibly they changed this behavior.
        // await driver.findElement('-android datamatcher', JSON.stringify({
        //   name: 'equalTo', args: 'Zamorano'
        // })).should.eventually.be.rejectedWith(/AmbiguousViewMatcherException/);

        // Narrow them down by making the root an adapter view
        const listOneEl = await driver.$(await driver.findElement('id', 'io.appium.android.apis:id/list1'));
        assert.ok(
          await listOneEl.findElement(
            '-android datamatcher',
            JSON.stringify({
              name: 'equalTo',
              args: 'Zamorano',
            }),
          ),
        );

        const listTwoEl = await driver.$(await driver.findElement('id', 'list2'));
        assert.ok(
          await listTwoEl.findElement(
            '-android datamatcher',
            JSON.stringify({
              name: 'equalTo',
              args: 'Zamorano',
            }),
          ),
        );
      });
    });
  });
  describe('by view matcher', function () {
    describe('View Matcher - dependent tests - Set 1', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
      });

      after(async function () {
        await deleteSession();
      });

      it('should fail to find elements with helpful error messages', async function () {
        const err = (await driver.findElement(
          '-android viewmatcher',
          JSON.stringify({
            name: 'hasEntry',
            args: ['title', 'A Fake Item'],
          }),
        )) as {error: string};
        assert.strictEqual(err.error, 'no such element');
      });

      it('should fail with invalid selector with helpful error messages', async function () {
        await assert.rejects(
          driver.findElement(
            '-android viewmatcher',
            JSON.stringify({
              name: 'notARealHamcrestMatcherStrategy',
              args: ['title', 'A Fake Item'],
            }),
          ),
        );
      });

      it('should allow "class" property with fully qualified className', async function () {
        await assert.rejects(
          driver.findElement(
            '-android viewmatcher',
            JSON.stringify({
              name: 'notARealHamcrestMatcherStrategy',
              args: ['title', 'A Fake Item'],
              class: 'org.hamcrest.Matchers',
            }),
          ),
        );
      });
    });

    describe('View Matcher - dependent tests - Set 2', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.execute('mobile:startActivity', {
          appPackage: 'io.appium.android.apis',
          appActivity: '.content.ExternalStorage',
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should find an element using view matcher', async function () {
        assert.ok(
          await driver.findElement(
            '-android viewmatcher',
            JSON.stringify({
              name: 'withText',
              args: 'Picture getExternalFilesDir',
              class: 'androidx.test.espresso.matcher.ViewMatchers',
            }),
          ),
        );
      });
      it('should allow multiple view matchers to be passed as args', async function () {
        assert.ok(
          await driver.findElement(
            '-android viewmatcher',
            JSON.stringify({
              name: 'withText',
              args: [
                {
                  name: 'containsString',
                  args: ' getExternalStoragePublicDirectory',
                  class: 'org.hamcrest.Matchers',
                },
              ],
              class: 'androidx.test.espresso.matcher.ViewMatchers',
            }),
          ),
        );
      });
    });

    describe('View Matcher - Dependent Tests - Set 3', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.execute('mobile:startActivity', {
          appPackage: 'io.appium.android.apis',
          appActivity: '.content.ClipboardSample',
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should be able to set a specific View as a root element when activity has multiple Views', async function () {
        // Finding by withText equalTo 'COPY TEXT' should be ambiguous, because there are three
        // items with the same matcher

        // TODO: maybe need to update the test since this didn't occur.
        // This error comes from Espresso framework itself, so possibly they changed this behavior.
        // await driver.findElement('-android viewmatcher', JSON.stringify({
        //   name: 'withText', args: 'Copy Text', class: 'androidx.test.espresso.matcher.ViewMatchers'
        // })).should.eventually.be.rejectedWith(/AmbiguousViewMatcherException/);

        const listTwoEl = await driver.$("//android.widget.LinearLayout[@index='2']");
        assert.ok(
          await listTwoEl.findElement(
            '-android viewmatcher',
            JSON.stringify({
              name: 'withText',
              args: 'Copy Text',
              class: 'androidx.test.espresso.matcher.ViewMatchers',
            }),
          ),
        );

        const listOneEl = await driver.$("//android.widget.LinearLayout[@index='1']");
        assert.ok(
          await listOneEl.findElement(
            '-android viewmatcher',
            JSON.stringify({
              name: 'withText',
              args: 'Copy Text',
              class: 'androidx.test.espresso.matcher.ViewMatchers',
            }),
          ),
        );
      });
    });
  });

  describe('Move element outside of screen into visible area', function () {
    before(async function () {
      driver = await initSession(APIDEMO_CAPS);
    });

    after(async function () {
      await deleteSession();
    });

    it('should move an element outside. the screen into the screen with find element', async function () {
      await scrollListToEnd(driver);
      // Espresso specific behavior.
      const el = await driver.$('~Views');
      await el.click();
      const imageEl = await driver.$('~ImageView');
      assert.ok(imageEl);
    });
  });
});
