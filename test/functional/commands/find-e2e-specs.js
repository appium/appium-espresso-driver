import { retryInterval } from 'asyncbox';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('find elements', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);
  });

  describe('elementByXPath', function () {

    describe('ElementByXpath - Dependent Test - Set 1', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
      });

      after(async function () {
        await deleteSession();
      });

      it(`should find an element by it's xpath`, async function () {
        let el = await driver.elementByXPath("//*[@text='Animation']");
        el.should.exist;
        await el.click();
        await driver.back();
      });
      it('should find multiple elements that match one xpath', async function () {
        let els = await driver.elementsByXPath('//android.widget.TextView');
        els.length.should.be.above(1);
        await els[0].click();
        await driver.back();
      });
      it('should get the first element of an xpath that matches more than one element', async function () {
        let el = await driver.elementByXPath('//android.widget.TextView');
        el.should.exist;
        await el.click();
        await driver.back();
      });
      it('should throw a stale element exception if clicking on element that does not exist', async function () {
        let el = await driver.elementByXPath("//*[@content-desc='Animation']");
        await el.click();
        await retryInterval(5, 1000, async () => await el.click().should.eventually.be.rejectedWith(/no longer exists /));
        await driver.back();
      });
      it('should get the isDisplayed attribute on the same element twice', async function () {
        let el = await driver.elementByXPath("//*[@content-desc='Animation']");
        await el.isDisplayed().should.eventually.be.true;
        await el.isDisplayed().should.eventually.be.true;
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

      it('should match an element if the element is off-screen but has an accessibility id', async function () {
        let el = await driver.elementByAccessibilityId('Views');
        await el.click();

        // Click on an element that is at the bottom of the list
        let moveToEl = await driver.elementByAccessibilityId('WebView');
        await moveToEl.click();
        await driver.back();
        await driver.back();
      });
      it('should test element equality', async function () {
        let el = await driver.elementByAccessibilityId('Views');
        let elAgain = await driver.elementByXPath("//*[@content-desc='Views']");
        let elNonMatch = await driver.elementByAccessibilityId('Preference');
        await el.equals(elAgain).should.eventually.be.true;
        await el.equals(elNonMatch).should.eventually.be.false;
      });
      // TODO: This test is very flakey. Need to inspect this.
      it.skip('should scroll element back into view if was scrolled out of view (regression test for https://github.com/appium/appium-espresso-driver/issues/276)', async function () {
        // If we find an element by 'contentDescription', scroll out of view of that element, we should be able to scroll it back into view, as long
        // as that element has a content description associated with an adapter item
        let el = await driver.elementByAccessibilityId('Views');
        await el.click();
        el = await driver.elementByAccessibilityId('Custom');
        await el.text().should.eventually.equal('Custom');
        let {value: element} = await driver.elementById('android:id/list');
        await driver.execute('mobile: swipe', {direction: 'up', element});
        await el.text().should.eventually.equal('Custom');
        await driver.back();
      });
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
        await driver.element('-android datamatcher', JSON.stringify({
          name: 'hasEntry', args: ['title', 'A Fake Item']
        })).should.eventually.be.rejectedWith(/NoSuchElement/);
      });
      it('should fail with invalid selector with helpful error messages', async function () {
        await driver.element('-android datamatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item']
        })).should.eventually.be.rejectedWith(/InvalidSelector/);
      });
      it('should allow "class" property with fully qualified className', async function () {
        await driver.element('-android datamatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item'], class: 'org.hamcrest.Matchers',
        })).should.eventually.be.rejectedWith(/InvalidSelector/);
      });
      it('should find an element using a data matcher', async function () {
        let el = await driver.element('-android datamatcher', JSON.stringify({
          name: 'hasEntry', args: ['title', 'Animation']
        }));
        await el.click();
        await driver.elementByAccessibilityId('Bouncing Balls').should.eventually.exist;
        await driver.back();
      });
      it('should find an offscreen element using a data matcher', async function () {
        let viewsEl = await driver.elementByAccessibilityId('Views');
        await viewsEl.click();
        let el = await driver.element('-android datamatcher', JSON.stringify({
          name: 'hasEntry', args: ['title', 'WebView3']
        }));
        await el.click();
        await driver.back();
        await driver.elementByAccessibilityId('Controls').should.eventually.exist;
        await driver.back();
      });
    });
    describe('Data Matcher - dependent tests - Set 2', function () {
      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.startActivity({
          'appPackage': 'io.appium.android.apis',
          'appActivity': '.view.SplitTouchView'
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should be able to set a specific AdapterView as a root element when activity has multiple AdapterViews', async function () {
        // Finding by adapter equalTo 'Zamorano' should be ambiguous, because there are two
        // adapter items with the same matcher
        await driver.element('-android datamatcher', JSON.stringify({
          name: 'equalTo', args: 'Zamorano'
        })).should.eventually.be.rejectedWith(/AmbiguousViewMatcherException/);

        // Narrow them down by making the root an adapter view
        const listOneEl = await driver.elementById('list1');
        await listOneEl.element('-android datamatcher', JSON.stringify({
          name: 'equalTo', args: 'Zamorano'
        })).should.eventually.exist;

        const listTwoEl = await driver.elementById('list2');
        await listTwoEl.element('-android datamatcher', JSON.stringify({
          name: 'equalTo', args: 'Zamorano'
        })).should.eventually.exist;
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
        await driver.element('-android viewmatcher', JSON.stringify({
          name: 'hasEntry', args: ['title', 'A Fake Item']
        })).should.eventually.be.rejectedWith(/NoMatchingView/);
      });

      it('should fail with invalid selector with helpful error messages', async function () {
        await driver.element('-android viewmatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item']
        })).should.eventually.be.rejectedWith(/InvalidSelector/);
      });

      it('should allow "class" property with fully qualified className', async function () {
        await driver.element('-android viewmatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item'], class: 'org.hamcrest.Matchers',
        })).should.eventually.be.rejectedWith(/InvalidSelector/);
      });
    });

    describe('View Matcher - dependent tests - Set 2', function () {

      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.startActivity({
          appPackage: 'io.appium.android.apis',
          appActivity: '.content.ExternalStorage'
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should find an element using view matcher', async function () {
        await driver.element('-android viewmatcher', JSON.stringify({
          name: 'withText',
          args: 'Picture getExternalFilesDir',
          class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;
      });
      it('should allow multiple view matchers to be passed as args', async function () {

        await driver.element('-android viewmatcher', JSON.stringify({
          name: 'withText',
          args: [
            {
              name: 'containsString',
              args: ' getExternalStoragePublicDirectory',
              class: 'org.hamcrest.Matchers'
            }
          ],
          class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;
      });

    });

    describe('View Matcher - Dependent Tests - Set 3', function () {

      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.startActivity({
          'appPackage': 'io.appium.android.apis',
          'appActivity': '.content.ClipboardSample'
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should be able to set a specific View as a root element when activity has multiple Views', async function () {
        // Finding by withText equalTo 'COPY TEXT' should be ambiguous, because there are threee
        // items with the same matcher

        await driver.element('-android viewmatcher', JSON.stringify({
          name: 'withText', args: 'Copy Text', class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.be.rejectedWith(/AmbiguousViewMatcherException/);

        const listTwoEl = await driver.elementByXPath("//android.widget.LinearLayout[@index='2']");
        await listTwoEl.element('-android viewmatcher', JSON.stringify({
          name: 'withText', args: 'Copy Text', class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;

        const listOneEl = await driver.elementByXPath("//android.widget.LinearLayout[@index='1']");
        await listOneEl.element('-android viewmatcher', JSON.stringify({
          name: 'withText', args: 'Copy Text', class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;
      });
    });
  });
});