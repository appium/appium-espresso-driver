import assert from 'node:assert/strict';
import {describe, it, before, after, beforeEach, afterEach} from 'node:test';

import type {Browser, ChainablePromiseElement} from 'webdriverio';

import {amendCapabilities, APIDEMO_CAPS} from '../desired.js';
import {initSession, deleteSession, E2E_TEST_TIMEOUT} from '../helpers/session.js';

describe('mobile', {timeout: E2E_TEST_TIMEOUT}, function () {
  let driver: Browser;

  before(async function () {
    driver = await initSession(
      amendCapabilities(APIDEMO_CAPS, {
        // FIXME: find proper version to fix skipped scenario in the 'should call the navigateTo method' test
        // 'appium:espressoBuildConfig': JSON.stringify({
        //   additionalAndroidTestDependencies: ['com.google.android.material:material:1.2.1']
        // })
      }),
    );
  });
  after(async function () {
    await deleteSession();
  });

  describe('mobile:swipe', function () {
    describe('with direction', {skip: Boolean(process.env.CI)}, function () {
      it('should swipe up and swipe down', async function () {
        const el = await driver.$('~Views');
        await el.click();
        assert.ok((await driver.getPageSource()).includes('Animation'));
        const element = await driver.$(await driver.findElement('id', 'android:id/list'));
        await driver.execute('mobile: swipe', {direction: 'up', elementId: element.elementId});
        assert.ok((await driver.getPageSource()).includes('Spinner'));
        await driver.execute('mobile: swipe', {direction: 'down', elementId: element.elementId});
        assert.ok((await driver.getPageSource()).includes('Animation'));
        await driver.back();
      });
    });
    describe('with GeneralSwipeAction', {skip: Boolean(process.env.CI)}, function () {
      beforeEach(async function () {
        const viewEl = await driver.$('~Views');
        await viewEl.click();
      });
      afterEach(async function () {
        await driver.back();
      });
      it('should call GeneralSwipeAction and use default params when params missing', async function () {
        const element = await driver.$(await driver.findElement('class name', 'android.widget.ListView'));
        await driver.execute('mobile: swipe', {elementId: element.elementId, swiper: 'slow'});
        // The swipe action shows up the app history, so should go back to the app view to proceed the test.
        // Android doesn't accept incoming actions on the espresso server with the app history.
        await driver.execute('mobile: shell', {command: 'input', args: ['keyevent', 4]});
        assert.ok((await driver.getPageSource()).includes('Animation'));
      });
      it('should call GeneralSwipeAction with provided parameters', async function () {
        const element = await driver.$(await driver.findElement('class name', 'android.widget.ListView'));
        await driver.execute('mobile: swipe', {
          elementId: element.elementId,
          swiper: 'slow',
          startCoordinates: 'BOTTOM_RIGHT',
          endCoordinates: 'TOP_RIGHT',
          precisionDescriber: 'FINGER',
        });
        // the swipe action shows up the app history, so should go back to the app view to proceed the test.
        // Android doesn't accept incoming actions on the espresso server with the app history.
        await driver.execute('mobile: shell', {command: 'input', args: ['keyevent', 4]});
        assert.ok((await driver.getPageSource()).includes('Animation'));
      });
      describe('failing swipe tests', function () {
        it('should not accept "direction" and "swiper". Must be one or the other', async function () {
          const element = await driver.$(await driver.findElement('class name', 'android.widget.ListView'));
          await assert.rejects(
            driver.execute('mobile: swipe', {
              elementId: element.elementId,
              swiper: 'slow',
              direction: 'down',
            }),
            /Cannot set both 'direction' and 'swiper' for swipe action/,
          );
        });
        it('should not accept if "direction" and "swiper" both are not set', async function () {
          const element = await driver.$(await driver.findElement('class name', 'android.widget.ListView'));
          await assert.rejects(
            driver.execute('mobile: swipe', {elementId: element.elementId}),
            /Must set one of 'direction' or 'swiper'/,
          );
        });

        // Iterate through a list of bad params
        for (const badParams of [
          {swiper: 'BAD'},
          {direction: 'sideWays'},
          {startCoordinates: {not: 'valid'}},
          {endCoordinates: 'NOT VALID'},
          {precisionDescriber: 'BUM'},
        ]) {
          it(`should reject bad parameters: ${JSON.stringify(badParams)}`, async function () {
            const element = await driver.$(await driver.findElement('class name', 'android.widget.ListView'));
            await assert.rejects(
              driver.execute('mobile: swipe', {
                elementId: element.elementId,
                swiper: 'slow',
                startCoordinates: 'BOTTOM_RIGHT',
                endCoordinates: 'TOP_RIGHT',
                precisionDescriber: 'FINGER',
                ...badParams,
              }),
            );
          });
        }
      });
    });
  });

  describe('mobile: openDrawer, mobile: closeDrawer', {skip: Boolean(process.env.CI)}, function () {
    it('should call these two commands but fail because element is not a drawer', async function () {
      // Testing for failures because ApiDemos app does not have a drawer to test on
      const el = await driver.$('~Views');
      await assert.rejects(
        driver.execute('mobile: openDrawer', {elementId: el.elementId, gravity: 1}),
        /open drawer with gravity/,
      );
      await assert.rejects(
        driver.execute('mobile: closeDrawer', {elementId: el.elementId, gravity: 1}),
        /close drawer with gravity/,
      );
    });
  });

  describe('mobile: setDate, mobile: setTime', {skip: Boolean(process.env.CI)}, function () {
    it('should set the date on a DatePicker', async function () {
      await driver.execute('mobile:startActivity', {
        appActivity: 'io.appium.android.apis.view.DateWidgets1',
      });
      const dateEl = await driver.$('~change the date');
      await dateEl.click();
      const datePicker = await driver.$(await driver.findElement('id', 'android:id/datePicker'));
      await driver.execute('mobile: setDate', {
        year: 2020,
        monthOfYear: 10,
        dayOfMonth: 25,
        elementId: datePicker.elementId,
      });
      const okButton = await driver.$(await driver.findElement('id', 'android:id/button1'));
      await okButton.click();
      const source = await driver.getPageSource();
      assert.strictEqual(source.includes('10-25-2020'), true);
      await driver.back();
    });
    it('should set the time on a timepicker', async function () {
      await driver.execute('mobile:startActivity', {
        appActivity: 'io.appium.android.apis.view.DateWidgets2',
      });
      const timeEl = await driver.$('//android.widget.TimePicker');
      await driver.execute('mobile: setTime', {
        hours: 10,
        minutes: 58,
        elementId: timeEl.elementId,
      });
      const source = await driver.getPageSource();
      assert.strictEqual(source.includes('10:58'), true);
      await driver.back();
    });
  });

  describe('mobile: navigateTo', {skip: Boolean(process.env.CI)}, function () {
    it('should validate params', async function () {
      const element = await driver.$('~Views');
      await assert.rejects(
        driver.execute('mobile: navigateTo', {elementId: element.elementId, menuItemId: -100}),
        /'menuItemId' must be a non-negative number/,
      );
      await assert.rejects(
        driver.execute('mobile: navigateTo', {elementId: element.elementId, menuItemId: 'fake'}),
        /'menuItemId' must be a non-negative number/,
      );
      await assert.rejects(driver.execute('mobile: navigateTo', {elementId: element.elementId}), /required/);
    });
    // dependency issue
    it.skip('should call the navigateTo method', async function () {
      // Testing for failures because ApiDemos app does not have a navigator view to test on
      const element = await driver.$('~Views');
      await assert.rejects(
        driver.execute('mobile: navigateTo', {elementId: element.elementId, menuItemId: 10}),
        /Could not navigate to menu item 10/,
      );
    });
  });

  describe('mobile: scrollToPage', {skip: Boolean(process.env.CI)}, function () {
    it('should validate the parameters', async function () {
      const el = await driver.$('~Views');
      await assert.rejects(
        driver.execute('mobile: scrollToPage', {
          elementId: el.elementId,
          scrollTo: 'SOMETHING DIFF',
        }),
        /must be one of /,
      );
      await assert.rejects(
        driver.execute('mobile: scrollToPage', {elementId: el.elementId, scrollToPage: -5}),
        /must be a non-negative integer/,
      );
      await assert.rejects(
        driver.execute('mobile: scrollToPage', {
          elementId: el.elementId,
          scrollToPage: 'NOT A NUMBER',
        }),
        /java.lang.NumberFormatException/,
      );
    });
    it('should call the scrollToPage method', async function () {
      // Testing for failures because ApiDemos app does not have a view pager to test on
      const el = await driver.$('~Views');
      await assert.rejects(
        driver.execute('mobile: scrollToPage', {elementId: el.elementId, scrollToPage: 1}),
        /Could not perform scroll to on element/,
      );
      await assert.rejects(
        driver.execute('mobile: scrollToPage', {elementId: el.elementId, scrollTo: 'left'}),
        /Could not perform scroll to on element/,
      );
      await assert.rejects(
        driver.execute('mobile: scrollToPage', {
          elementId: el.elementId,
          scrollTo: 'left',
          smoothScroll: true,
        }),
        /Could not perform scroll to on element/,
      );
    });
  });

  describe('mobile:uiautomator', {skip: Boolean(process.env.CI)}, function () {
    it('should be able to find and take action on all uiObjects', async function () {
      const text = await driver.execute('mobile: uiautomator', {
        strategy: 'clazz',
        locator: 'android.widget.TextView',
        action: 'getText',
      });
      assert.ok((text as string).includes('Views'));
    });
    it('should be able to find and take action on uiObject with given index', async function () {
      const text = await driver.execute('mobile: uiautomator', {
        strategy: 'textContains',
        locator: 'Views',
        index: 0,
        action: 'getText',
      });
      assert.deepStrictEqual(text, ['Views']);
    });
  });
  describe('mobile: clickAction', {skip: Boolean(process.env.CI)}, function () {
    let viewEl: ChainablePromiseElement;

    beforeEach(async function () {
      viewEl = await driver.$('~Views');
    });

    it('should click on an element and use default parameters', async function () {
      await driver.execute('mobile: clickAction', {elementId: viewEl.elementId});
      assert.ok((await driver.getPageSource()).includes('Animation'));
      await driver.back();
    });
    it('should click on an element and accept parameters', async function () {
      await driver.execute('mobile: clickAction', {
        elementId: viewEl.elementId,
        tapper: 'LoNg',
        coordinatesProvider: 'BoTtOm_rIgHt',
        precisionDescriber: 'tHuMb',
        inputDevice: 0,
        buttonState: 0,
      });
      assert.ok((await driver.getPageSource()).includes('Animation'));
      await driver.back();
    });

    const badParams: Array<[string, unknown, RegExp]> = [
      ['tapper', 'BaD TAPPER', /is not a valid 'tapper' type/],
      ['coordinatesProvider', 'BAD_COORDINATES_prOVIDER', /is not a valid 'coordinatesProvider' type/],
      ['precisionDescriber', 'BaD PrEcIsIoN DeScRiBeR', /is not a valid 'precisionDescriber' type/],
      ['inputDevice', 'wrong', /NumberFormatException/],
      ['buttonState', 'wrong', /NumberFormatException/],
    ];

    for (const [name, value, error] of badParams) {
      it(`should fail properly if provide an invalid parameter: '${name}'`, async function () {
        await assert.rejects(
          driver.execute('mobile: clickAction', {
            elementId: viewEl.elementId,
            ...({[name]: [value]} as Record<string, unknown>),
          }),
          error,
        );
      });
    }
  });

  describe('mobile: backdoor', {skip: Boolean(process.env.CI)}, function () {
    it('should get element type face', async function () {
      const element = await driver.$('~Views');
      // Below returns like: {"mStyle"=>0, "mSupportedAxes"=>nil, "mWeight"=>400, "native_instance"=>131438067610240}
      assert.strictEqual(
        await driver.execute('mobile: backdoor', {
          target: 'element',
          elementId: element.elementId,
          methods: [{name: 'getTypeface'}, {name: 'getStyle'}],
        }),
        0,
      );
    });
  });
});
