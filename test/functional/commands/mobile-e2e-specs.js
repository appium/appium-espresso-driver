import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';

chai.should();
chai.use(chaiAsPromised);

describe('mobile', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  describe('mobile:swipe', function () {
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
      await driver.startActivity({appPackage: 'io.appium.android.apis', appActivity: 'io.appium.android.apis.view.DateWidgets1'});
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
      await driver.startActivity({appPackage: 'io.appium.android.apis', appActivity: 'io.appium.android.apis.view.DateWidgets2'});
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
      await driver.execute('mobile: navigateTo', {element, menuItemId: "fake"}).should.eventually.be.rejectedWith(/'menuItemId' must be a non-negative number/);
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
      //await driver.execute('mobile: scrollToPage', {element: el}).should.eventually.be.rejectedWith(/Must provide either/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollTo: "SOMETHING DIFF"}).should.eventually.be.rejectedWith(/must be one of /);
      await driver.execute('mobile: scrollToPage', {element: el, scrollToPage: -5}).should.eventually.be.rejectedWith(/must be a non-negative integer/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollToPage: "NOT A NUMBER"}).should.eventually.be.rejectedWith(/must be a non-negative integer/);
    });
    it('should call the scrollToPage method', async function () {
      // Testing for failures because ApiDemos app does not have a view pager to test on
      let el = await driver.elementByAccessibilityId('Views');
      await driver.execute('mobile: scrollToPage', {element: el, scrollToPage: 1}).should.eventually.be.rejectedWith(/Could not perform scroll to on element/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollTo: 'left'}).should.eventually.be.rejectedWith(/Could not perform scroll to on element/);
      await driver.execute('mobile: scrollToPage', {element: el, scrollTo: 'left', smoothScroll: true}).should.eventually.be.rejectedWith(/Could not perform scroll to on element/);
    });
  });
});
