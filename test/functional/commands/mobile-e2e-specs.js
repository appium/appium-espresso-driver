import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('mobile', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    driver = await initSession(Object.assign({}, APIDEMO_CAPS, {
      espressoBuildConfig: JSON.stringify({
        additionalAndroidTestDependencies: ['com.google.android.material:material:1.2.1']
      })
    }));
  });
  after(async function () {
    await deleteSession();
  });

  describe('mobile:swipe', function () {
    describe('with direction', function () {
      it('should swipe up and swipe down', async function () {
        let el = await driver.elementByAccessibilityId('Views');
        await el.click();
        await driver.source().should.eventually.contain('Animation');
        let {value: element} = await driver.elementById('android:id/list');
        await driver.execute('mobile: swipe', {direction: 'up', element});
        await driver.source().should.eventually.contain('Spinner');
        await driver.execute('mobile: swipe', {direction: 'down', element});
        await driver.source().should.eventually.contain('Animation');
        await driver.back();
      });
    });
    describe('with GeneralSwipeAction', function () {
      let viewEl;
      beforeEach(async function () {
        viewEl = await driver.elementByAccessibilityId('Views');
        await viewEl.click();
      });
      afterEach(async function () {
        await driver.back();
      });
      it('should call GeneralSwipeAction and use default params when params missing', async function () {
        let element = await driver.elementByClassName('android.widget.ListView');
        await driver.execute('mobile: swipe', {element, swiper: 'slow'});
        await driver.source().should.eventually.contain('Animation');
      });
      it('should call GeneralSwipeAction with provided parameters', async function () {
        let element = await driver.elementByClassName('android.widget.ListView');
        await driver.execute('mobile: swipe', {
          element,
          swiper: 'slow',
          startCoordinates: 'BOTTOM_RIGHT',
          endCoordinates: 'TOP_RIGHT',
          precisionDescriber: 'FINGER',
        });
        await driver.source().should.eventually.contain('Animation');
      });
      describe('failing swipe tests', function () {
        it('should not accept "direction" and "swiper". Must be one or the other', async function () {
          let element = await driver.elementByClassName('android.widget.ListView');
          await driver.execute('mobile: swipe', {element, swiper: 'slow', direction: 'down'})
            .should.eventually.be.rejectedWith(/Cannot set both 'direction' and 'swiper' for swipe action/);
        });
        it('should not accept if "direction" and "swiper" both are not set', async function () {
          let element = await driver.elementByClassName('android.widget.ListView');
          await driver.execute('mobile: swipe', {element})
            .should.eventually.be.rejectedWith(/Must set one of 'direction' or 'swiper'/);

        });

        // Iterate through a list of bad params
        for (let badParams of [
          {swiper: 'BAD'},
          {direction: 'sideWays'},
          {startCoordinates: {not: 'valid'}},
          {endCoordinates: 'NOT VALID'},
          {precisionDescriber: 'BUM'},
        ]) {
          it(`should reject bad parameters: ${JSON.stringify(badParams)}`, async function () {
            let element = await driver.elementByClassName('android.widget.ListView');
            await driver.execute('mobile: swipe', {
              element,
              swiper: 'slow',
              startCoordinates: 'BOTTOM_RIGHT',
              endCoordinates: 'TOP_RIGHT',
              precisionDescriber: 'FINGER',
              ...badParams,
            }).should.eventually.be.rejected;
          });
        }
      });
    });
  });

  describe('mobile: openDrawer, mobile: closeDrawer', function () {
    it('should call these two commands but fail because element is not a drawer', async function () {
      // Testing for failures because ApiDemos app does not have a drawer to test on
      let el = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: openDrawer', {element: el, gravity: 1}).should.eventually.be.rejectedWith(/open drawer with gravity/);
      await driver.execute('mobile: closeDrawer', {element: el, gravity: 1}).should.eventually.be.rejectedWith(/close drawer with gravity/);
    });
  });

  describe('mobile: setDate, mobile: setTime', function () {
    it('should set the date on a DatePicker', async function () {
      await driver.startActivity({
        appPackage: 'io.appium.android.apis',
        appActivity: 'io.appium.android.apis.view.DateWidgets1',
      });
      let dateEl = await driver.elementByAccessibilityId('change the date');
      await dateEl.click();
      let datePicker = await driver.elementById('android:id/datePicker');
      await driver.execute('mobile: setDate', {year: 2020, monthOfYear: 10, dayOfMonth: 25, element: datePicker});
      let okButton = await driver.elementById('android:id/button1');
      await okButton.click();
      let source = await driver.source();
      source.includes('10-25-2020').should.be.true;
      await driver.back();
    });
    it('should set the time on a timepicker', async function () {
      await driver.startActivity({
        appPackage: 'io.appium.android.apis',
        appActivity: 'io.appium.android.apis.view.DateWidgets2',
      });
      let timeEl = await driver.elementByXPath('//android.widget.TimePicker');
      await driver.execute('mobile: setTime', {hours: 10, minutes: 58, element: timeEl});
      let source = await driver.source();
      source.includes('10:58').should.be.true;
      await driver.back();
    });
  });

  describe('mobile: navigateTo', function () {
    it('should validate params', async function () {
      let element = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: navigateTo', {element, menuItemId: -100}).should.eventually.be.rejectedWith(/'menuItemId' must be a non-negative number/);
      await driver.execute('mobile: navigateTo', {element, menuItemId: 'fake'}).should.eventually.be.rejectedWith(/'menuItemId' must be a non-negative number/);
      await driver.execute('mobile: navigateTo', {element}).should.eventually.be.rejectedWith(/required/);
    });
    it('should call the navigateTo method', async function () {
      // Testing for failures because ApiDemos app does not have a navigator view to test on
      let element = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: navigateTo', {element, menuItemId: 10}).should.eventually.be.rejectedWith(/Could not navigate to menu item 10/);
    });
  });

  describe('mobile: scrollToPage', function () {
    it('should validate the parameters', async function () {
      let el = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: scrollToPage', {element: el, scrollTo: 'SOMETHING DIFF'}).should.eventually.be.rejectedWith(/must be one of /);
      await driver.execute('mobile: scrollToPage', {element: el, scrollToPage: -5}).should.eventually.be.rejectedWith(/must be a non-negative integer/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollToPage: 'NOT A NUMBER'}).should.eventually.be.rejectedWith(/must be a non-negative integer/);
    });
    it('should call the scrollToPage method', async function () {
      // Testing for failures because ApiDemos app does not have a view pager to test on
      let el = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: scrollToPage', {element: el, scrollToPage: 1}).should.eventually.be.rejectedWith(/Could not perform scroll to on element/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollTo: 'left'}).should.eventually.be.rejectedWith(/Could not perform scroll to on element/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollTo: 'left', smoothScroll: true}).should.eventually.be.rejectedWith(/Could not perform scroll to on element/);
    });
  });

  describe('mobile:uiautomator', function () {
    it('should be able to find and take action on all uiObjects', async function () {
      const text = await driver.execute('mobile: uiautomator', {strategy: 'clazz', locator: 'android.widget.TextView', action: 'getText'});
      text.should.include('Views');
    });
    it('should be able to find and take action on uiObject with given index', async function () {
      const text = await driver.execute('mobile: uiautomator', {strategy: 'textContains', locator: 'Views', index: 0, action: 'getText'});
      text.should.eql(['Views']);
    });
  });
  describe('mobile: clickAction', function () {
    let viewEl;
    beforeEach(async function () {
      viewEl = await driver.elementByAccessibilityId('Views');
    });

    it('should click on an element and use default parameters', async function () {
      await driver.execute('mobile: clickAction', {element: viewEl});
      await driver.source().should.eventually.contain('Animation');
      await driver.back();
    });
    it('should click on an element and accept parameters', async function () {
      await driver.execute('mobile: clickAction', {
        element: viewEl,
        tapper: 'LoNg',
        coordinatesProvider: 'BoTtOm_rIgHt',
        precisionDescriber: 'tHuMb',
        inputDevice: 0,
        buttonState: 0,
      });
      await driver.source().should.eventually.contain('Animation');
      await driver.back();
    });

    const badParams = [
      ['tapper', 'BaD TAPPER', /is not a valid 'tapper' type/],
      ['coordinatesProvider', 'BAD_COORDINATES_prOVIDER', /is not a valid 'coordinatesProvider' type/],
      ['precisionDescriber', 'BaD PrEcIsIoN DeScRiBeR', /is not a valid 'precisionDescriber' type/],
      ['inputDevice', 'wrong', /NumberFormatException/],
      ['buttonState', 'wrong', /NumberFormatException/],
    ];

    for (let [name, value, error] of badParams) {
      it(`should fail properly if provide an invalid parameter: '${name}'`, async function () {
        await driver.execute('mobile: clickAction', {
          element: viewEl,
          ...{[name]: [value]}
        }).should.eventually.be.rejectedWith(error);
      });
    }
  });

  describe('mobile: backdoor', function () {
    it('should get element type face', async function () {
      const element = await driver.elementByAccessibilityId('Views');
      // Below returns like: {"mStyle"=>0, "mSupportedAxes"=>nil, "mWeight"=>400, "native_instance"=>131438067610240}
      await driver.execute('mobile: backdoor', {
        target: 'element', elementId: element.value, methods: [{ name: 'getTypeface' }, { name: 'getStyle' }]
      }
      ).should.eventually.eql(0);
    });
  });
});
