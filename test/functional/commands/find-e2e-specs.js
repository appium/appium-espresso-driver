import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { retryInterval } from 'asyncbox';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);

describe('find elements', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });
  describe('elementByXPath', function () {
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

  describe('by data matcher', function () {
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
  });
});