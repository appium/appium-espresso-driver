import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, MOCHA_TIMEOUT} from '../helpers/session';
import {amendCapabilities, APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('mobile', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;

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
    describe('with direction', function () {
      before(function () {
        if (process.env.CI) {
          // CI env is flaky because of the bad emulator performance
          this.skip();
        }
      });

      it('should swipe up and swipe down', async function () {
        const el = await driver.$('~Views');
        await el.click();
        await expect(driver.getPageSource()).to.eventually.contain('Animation');
        const element = await driver.$(await driver.findElement('id', 'android:id/list'));
        await driver.execute('mobile: swipe', {direction: 'up', elementId: element.elementId});
        await expect(driver.getPageSource()).to.eventually.contain('Spinner');
        await driver.execute('mobile: swipe', {direction: 'down', elementId: element.elementId});
        await expect(driver.getPageSource()).to.eventually.contain('Animation');
        await driver.back();
      });
    });
    describe('with GeneralSwipeAction', function () {
      before(function () {
        if (process.env.CI) {
          // CI env is flaky because of the bad emulator performance
          this.skip();
        }
      });

      beforeEach(async function () {
        const viewEl = await driver.$('~Views');
        await viewEl.click();
      });
      afterEach(async function () {
        await driver.back();
      });
      it('should call GeneralSwipeAction and use default params when params missing', async function () {
        const element = await driver.$(
          await driver.findElement('class name', 'android.widget.ListView'),
        );
        await driver.execute('mobile: swipe', {elementId: element.elementId, swiper: 'slow'});
        // The swipe action shows up the app history, so should go back to the app view to proceed the test.
        // Android doesn't accept incoming actions on the espresso server with the app history.
        await driver.execute('mobile: shell', {command: 'input', args: ['keyevent', 4]});
        await expect(driver.getPageSource()).to.eventually.contain('Animation');
      });
      it('should call GeneralSwipeAction with provided parameters', async function () {
        const element = await driver.$(
          await driver.findElement('class name', 'android.widget.ListView'),
        );
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
        await expect(driver.getPageSource()).to.eventually.contain('Animation');
      });
      describe('failing swipe tests', function () {
        it('should not accept "direction" and "swiper". Must be one or the other', async function () {
          const element = await driver.$(
            await driver.findElement('class name', 'android.widget.ListView'),
          );
          await expect(
            driver.execute('mobile: swipe', {
              elementId: element.elementId,
              swiper: 'slow',
              direction: 'down',
            }),
          ).to.be.rejectedWith(/Cannot set both 'direction' and 'swiper' for swipe action/);
        });
        it('should not accept if "direction" and "swiper" both are not set', async function () {
          const element = await driver.$(
            await driver.findElement('class name', 'android.widget.ListView'),
          );
          await expect(
            driver.execute('mobile: swipe', {elementId: element.elementId}),
          ).to.be.rejectedWith(/Must set one of 'direction' or 'swiper'/);
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
            const element = await driver.$(
              await driver.findElement('class name', 'android.widget.ListView'),
            );
            await expect(
              driver.execute('mobile: swipe', {
                elementId: element.elementId,
                swiper: 'slow',
                startCoordinates: 'BOTTOM_RIGHT',
                endCoordinates: 'TOP_RIGHT',
                precisionDescriber: 'FINGER',
                ...badParams,
              }),
            ).to.be.rejected;
          });
        }
      });
    });
  });

  describe('mobile: openDrawer, mobile: closeDrawer', function () {
    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

    it('should call these two commands but fail because element is not a drawer', async function () {
      // Testing for failures because ApiDemos app does not have a drawer to test on
      const el = await driver.$('~Views');
      await expect(
        driver.execute('mobile: openDrawer', {elementId: el.elementId, gravity: 1}),
      ).to.be.rejectedWith(/open drawer with gravity/);
      await expect(
        driver.execute('mobile: closeDrawer', {elementId: el.elementId, gravity: 1}),
      ).to.be.rejectedWith(/close drawer with gravity/);
    });
  });

  describe('mobile: setDate, mobile: setTime', function () {
    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

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
      expect(source.includes('10-25-2020')).to.be.true;
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
      expect(source.includes('10:58')).to.be.true;
      await driver.back();
    });
  });

  describe('mobile: navigateTo', function () {
    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

    it('should validate params', async function () {
      const element = await driver.$('~Views');
      await expect(
        driver.execute('mobile: navigateTo', {elementId: element.elementId, menuItemId: -100}),
      ).to.be.rejectedWith(/'menuItemId' must be a non-negative number/);
      await expect(
        driver.execute('mobile: navigateTo', {elementId: element.elementId, menuItemId: 'fake'}),
      ).to.be.rejectedWith(/'menuItemId' must be a non-negative number/);
      await expect(
        driver.execute('mobile: navigateTo', {elementId: element.elementId}),
      ).to.be.rejectedWith(/required/);
    });
    // dependency issue
    it.skip('should call the navigateTo method', async function () {
      // Testing for failures because ApiDemos app does not have a navigator view to test on
      const element = await driver.$('~Views');
      await expect(
        driver.execute('mobile: navigateTo', {elementId: element.elementId, menuItemId: 10}),
      ).to.be.rejectedWith(/Could not navigate to menu item 10/);
    });
  });

  describe('mobile: scrollToPage', function () {
    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

    it('should validate the parameters', async function () {
      const el = await driver.$('~Views');
      await expect(
        driver.execute('mobile: scrollToPage', {
          elementId: el.elementId,
          scrollTo: 'SOMETHING DIFF',
        }),
      ).to.be.rejectedWith(/must be one of /);
      await expect(
        driver.execute('mobile: scrollToPage', {elementId: el.elementId, scrollToPage: -5}),
      ).to.be.rejectedWith(/must be a non-negative integer/);
      await expect(
        driver.execute('mobile: scrollToPage', {
          elementId: el.elementId,
          scrollToPage: 'NOT A NUMBER',
        }),
      ).to.be.rejectedWith(/java.lang.NumberFormatException/);
    });
    it('should call the scrollToPage method', async function () {
      // Testing for failures because ApiDemos app does not have a view pager to test on
      const el = await driver.$('~Views');
      await expect(
        driver.execute('mobile: scrollToPage', {elementId: el.elementId, scrollToPage: 1}),
      ).to.be.rejectedWith(/Could not perform scroll to on element/);
      await expect(
        driver.execute('mobile: scrollToPage', {elementId: el.elementId, scrollTo: 'left'}),
      ).to.be.rejectedWith(/Could not perform scroll to on element/);
      await expect(
        driver.execute('mobile: scrollToPage', {
          elementId: el.elementId,
          scrollTo: 'left',
          smoothScroll: true,
        }),
      ).to.be.rejectedWith(/Could not perform scroll to on element/);
    });
  });

  describe('mobile:uiautomator', function () {
    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

    it('should be able to find and take action on all uiObjects', async function () {
      const text = await driver.execute('mobile: uiautomator', {
        strategy: 'clazz',
        locator: 'android.widget.TextView',
        action: 'getText',
      });
      expect(text).to.include('Views');
    });
    it('should be able to find and take action on uiObject with given index', async function () {
      const text = await driver.execute('mobile: uiautomator', {
        strategy: 'textContains',
        locator: 'Views',
        index: 0,
        action: 'getText',
      });
      expect(text).to.eql(['Views']);
    });
  });
  describe('mobile: clickAction', function () {
    let viewEl;

    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

    beforeEach(async function () {
      viewEl = await driver.$('~Views');
    });

    it('should click on an element and use default parameters', async function () {
      await driver.execute('mobile: clickAction', {elementId: viewEl.elementId});
      await expect(driver.getPageSource()).to.eventually.contain('Animation');
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
      await expect(driver.getPageSource()).to.eventually.contain('Animation');
      await driver.back();
    });

    const badParams: Array<[string, unknown, RegExp]> = [
      ['tapper', 'BaD TAPPER', /is not a valid 'tapper' type/],
      [
        'coordinatesProvider',
        'BAD_COORDINATES_prOVIDER',
        /is not a valid 'coordinatesProvider' type/,
      ],
      ['precisionDescriber', 'BaD PrEcIsIoN DeScRiBeR', /is not a valid 'precisionDescriber' type/],
      ['inputDevice', 'wrong', /NumberFormatException/],
      ['buttonState', 'wrong', /NumberFormatException/],
    ];

    for (const [name, value, error] of badParams) {
      it(`should fail properly if provide an invalid parameter: '${name}'`, async function () {
        await expect(
          driver.execute('mobile: clickAction', {
            elementId: viewEl.elementId,
            ...({[name]: [value]} as Record<string, unknown>),
          }),
        ).to.be.rejectedWith(error);
      });
    }
  });

  describe('mobile: backdoor', function () {
    before(function () {
      if (process.env.CI) {
        // CI env is flaky because of the bad emulator performance
        this.skip();
      }
    });

    it('should get element type face', async function () {
      const element = await driver.$('~Views');
      // Below returns like: {"mStyle"=>0, "mSupportedAxes"=>nil, "mWeight"=>400, "native_instance"=>131438067610240}
      await expect(
        driver.execute('mobile: backdoor', {
          target: 'element',
          elementId: element.elementId,
          methods: [{name: 'getTypeface'}, {name: 'getStyle'}],
        }),
      ).to.eventually.equal(0);
    });
  });
});
