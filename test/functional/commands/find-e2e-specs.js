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
        el.should.exist;
        await el.click();
        await driver.back();
      });
      it('should find multiple elements that match one xpath', async function () {
        const els = await driver.$$('//android.widget.TextView');
        els.length.should.be.above(1);
        await els[0].click();
        await driver.back();
      });
      it('should get the first element of an xpath that matches more than one element', async function () {
        const el = await driver.$('//android.widget.TextView');
        el.should.exist;
        await el.click();
        await driver.back();
      });
      it('should throw a stale element exception if clicking on element that does not exist', async function () {
        const el = await driver.$("//*[@content-desc='Animation']");
        await el.click();
        try {
          await driver.elementClick(el.elementId);
          throw Error('Should raise an error before this line.');
        } catch (err) {
          err.name.should.eq('stale element reference');
        }
        await driver.back();
      });
      it('should get the isElementDisplayed attribute on the same element twice', async function () {
        const el = await driver.$("//*[@content-desc='Animation']");
        await driver.isElementDisplayed(el.elementId).should.eventually.be.true;
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
        if (process.env.CI) {
          // CI env is flaky
          this.skip();
        }
        const el = await driver.$('~Views');
        await el.click();

        // Click on an element that is at the bottom of the list
        const moveToEl = await driver.$('~WebView');
        await driver.elementClick(moveToEl.elementId);
        await driver.back();
        await driver.back();
      });
      // TODO: Need to check if this is still valid
      it.skip('should test element equality', async function () {
        const el = await driver.$('~Views');
        const elAgain = await driver.$("//*[@content-desc='Views']");
        const elNonMatch = await driver.$('~Preference');
        await el.isEqual(elAgain).should.eventually.be.true;
        await el.isEqual(elNonMatch).should.eventually.be.false;
      });
      // TODO: This test is very flakey. Need to inspect this.
      it.skip('should scroll element back into view if was scrolled out of view (regression test for https://github.com/appium/appium-espresso-driver/issues/276)', async function () {
        // If we find an element by 'contentDescription', scroll out of view of that element, we should be able to scroll it back into view, as long
        // as that element has a content description associated with an adapter item
        let el = await driver.$('~Views');
        await el.click();
        el = await driver.$('~Custom');
        await el.text().should.eventually.equal('Custom');
        let {value: element} = await driver.elementById('android:id/list');
        await driver.execute('mobile: swipe', {direction: 'up', element});
        await el.text().should.eventually.equal('Custom');
        await driver.back();
      });
    });

  });
  describe('by data matcher', function () {

    before(function () {
      // Lower versions' emulators on CI were flaky.
      if (parseInt(process.env.ANDROID_SDK_VERSION, 10) <= 25) {
        this.skip();
      }
    });

    describe('Data Matcher - dependent tests - Set 1', function () {

      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
      });

      after(async function () {
        await deleteSession();
      });
      it('should fail to find elements with helpful error messages', async function () {
        const err = await driver.findElement('-android datamatcher', JSON.stringify({
          name: 'hasEntry', args: ['title', 'A Fake Item']
        }));
        // webdriverio didn't raise exception for no such element here.
        err.error.should.eq('no such element');
      });
      it('should fail with invalid selector with helpful error messages', async function () {
        await driver.findElement('-android datamatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item']
        })).should.eventually.be.rejectedWith(/Not a valid selector/);
      });
      it('should allow "class" property with fully qualified className', async function () {
        await driver.findElement('-android datamatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item'], class: 'org.hamcrest.Matchers',
        })).should.eventually.be.rejectedWith(/Not a valid selector/);
      });
      it('should find an element using a data matcher', async function () {
        const el = await driver.$(
          await driver.findElement('-android datamatcher', JSON.stringify({
            name: 'hasEntry', args: ['title', 'Animation']
          }))
        );
        await el.click();
        await driver.$('~Bouncing Balls');
        await driver.back();
      });
      it('should find an offscreen element using a data matcher', async function () {
        let viewsEl = await driver.$('~Views');
        await viewsEl.click();
        const el = await driver.$(
          await driver.findElement('-android datamatcher', JSON.stringify({
            name: 'hasEntry', args: ['title', 'WebView3']
          }))
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

        // TODO: maybe need to update the test since this didn't occur.
        // This error comes from Espresso framework itself, so possibly they changed this behavior.
        // await driver.findElement('-android datamatcher', JSON.stringify({
        //   name: 'equalTo', args: 'Zamorano'
        // })).should.eventually.be.rejectedWith(/AmbiguousViewMatcherException/);

        // Narrow them down by making the root an adapter view
        const listOneEl = await driver.$(await driver.findElement('id', 'io.appium.android.apis:id/list1'));
        await listOneEl.findElement('-android datamatcher', JSON.stringify({
          name: 'equalTo', args: 'Zamorano'
        })).should.eventually.exist;

        const listTwoEl = await driver.$(await driver.findElement('id', 'list2'));
        await listTwoEl.findElement('-android datamatcher', JSON.stringify({
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
        const err = await driver.findElement('-android viewmatcher', JSON.stringify({
          name: 'hasEntry', args: ['title', 'A Fake Item']
        }));
        // webdriverio didn't raise exception for no such element here.
        err.error.should.eq('no such element');
      });

      it('should fail with invalid selector with helpful error messages', async function () {
        await driver.findElement('-android viewmatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item']
        })).should.eventually.be.rejected;
      });

      it('should allow "class" property with fully qualified className', async function () {
        await driver.findElement('-android viewmatcher', JSON.stringify({
          name: 'notARealHamcrestMatcherStrategy', args: ['title', 'A Fake Item'], class: 'org.hamcrest.Matchers',
        })).should.eventually.be.rejected;
      });
    });

    describe('View Matcher - dependent tests - Set 2', function () {

      before(async function () {
        driver = await initSession(APIDEMO_CAPS);
        await driver.execute('mobile:startActivity', {
          appPackage: 'io.appium.android.apis',
          appActivity: '.content.ExternalStorage'
        });
      });

      after(async function () {
        await deleteSession();
      });

      it('should find an element using view matcher', async function () {
        await driver.findElement('-android viewmatcher', JSON.stringify({
          name: 'withText',
          args: 'Picture getExternalFilesDir',
          class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;
      });
      it('should allow multiple view matchers to be passed as args', async function () {

        await driver.findElement('-android viewmatcher', JSON.stringify({
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
        await driver.execute('mobile:startActivity', {
          'appPackage': 'io.appium.android.apis',
          'appActivity': '.content.ClipboardSample'
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
        await listTwoEl.findElement('-android viewmatcher', JSON.stringify({
          name: 'withText', args: 'Copy Text', class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;

        const listOneEl = await driver.$("//android.widget.LinearLayout[@index='1']");
        await listOneEl.findElement('-android viewmatcher', JSON.stringify({
          name: 'withText', args: 'Copy Text', class: 'androidx.test.espresso.matcher.ViewMatchers'
        })).should.eventually.exist;
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

      // Espresso specific behavior.
      const el = await driver.$('~Views');
      await el.click();
      const imageEl = await driver.$('~ImageView');
      imageEl.should.exist;
    });
  });
});
