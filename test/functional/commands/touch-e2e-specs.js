import axios from 'axios';
import B from 'bluebird';
import _ from 'lodash';
import {
  initSession, deleteSession, HOST, PORT,
  MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


describe('touch actions -', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let sessionId;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    driver = await initSession(APIDEMO_CAPS);
    sessionId = await driver.getSessionId();
  });
  after(async function () {
    await deleteSession();
  });

  async function startListActivity () {
    await driver.startActivity({
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.List5',
    });
  }

  async function startFingerPaintActivity () {
    await driver.startActivity({
      appPackage: 'io.appium.android.apis',
      appActivity: '.graphics.FingerPaint',
    });
  }

  async function startSplitTouchActivity () {
    await driver.startActivity({
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.SplitTouchView',
    });
  }

  async function startDragAndDropActivity () {
    await driver.startActivity({
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.DragAndDropDemo',
    });
  }

  async function startTextSwitcherActivity () {
    await driver.startActivity({
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.TextSwitcher1',
    });
  }

  async function getScrollData () {
    const els = await driver.elementsByClassName('android.widget.TextView');

    // the last element is the title of the view, and often the
    // second-to-last is actually off the screen
    const startEl = els[els.length - 3];
    const {x: startX, y: startY} = await startEl.getLocation();

    const endEl = _.first(els);
    const {x: endX, y: endY} = await endEl.getLocation();

    return {startX, startY, endX, endY, startEl, endEl};
  }

  async function assertScroll () {
    await driver.elementByXPath("//*[@text='Abbaye de Belloc']")
      .should.eventually.be.rejectedWith(/NoSuchElement/);
  }

  let idCounter = 0;

  const performAction = async function (pointerType, ...actionsArrays) {
    const actionsRoot = [];

    for (let actions of actionsArrays) {
      actionsRoot.push({
        type: 'pointer',
        id: `id_${idCounter++}`,
        parameters: {
          pointerType
        },
        actions,
      });
    }

    await axios({
      method: 'POST',
      url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/actions`,
      data: {actions: actionsRoot},
    });

  };

  const performTouchAction = async function (...actionsArrays) {
    return await performAction('touch', ...actionsArrays);
  };

  describe('fingerpaint', function () {
    beforeEach(startFingerPaintActivity);

    it('should draw the letter L on fingerpaint', async function () {
      const canvas = await driver.elementById('android:id/content');
      const {x, y} = await canvas.getLocation();
      const {height, width} = await canvas.getSize();

      const startX = x + 10;
      const startY = y + 10;
      const endX = x + width - 10;
      const endY = y + Math.round(height / 2);

      const touchActions = [
        {type: 'pointerMove', duration: 1000, x: startX, y: startY},
        {type: 'pointerDown', button: 0},
        {type: 'pointerMove', duration: 1000, x: startX, y: endY},
        {type: 'pause', duration: 1000},
        {type: 'pointerMove', duration: 1000, x: endX, y: endY},
        {type: 'pointerCancel', button: 0},
      ];
      await performTouchAction(touchActions);
    });

    it('should draw two parallel lines on fingerpaint', async function () {
      const canvas = await driver.elementById('android:id/content');
      const {x, y} = await canvas.getLocation();
      const {height, width} = await canvas.getSize();

      const touchActions = [10, width - 10].reduce(function (touchActions, xOffset) {
        touchActions.push([
          {type: 'pointerMove', x: x + xOffset, y: y + 10},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 2000, x: x + xOffset, y: y + Math.round(height / 2)},
          {type: 'pointerUp', button: 0},
        ]);
        return touchActions;
      }, []);
      await performTouchAction(...touchActions);
    });
  });

  describe('scrolling/swiping', function () {
    describe('single', function () {
      this.retries(2);
      beforeEach(startListActivity);

      it('should scroll up menu', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        const actions = [
          {type: 'pointerMove', duration: 0, x: startX + 5, y: startY + 5},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 200, x: endX + 5, y: endY + 5},
          {type: 'pointerUp', button: 0}
        ];
        await performTouchAction(actions);

        await assertScroll();
      });

      it('should swipe up menu', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        const actions = [
          {type: 'pointerMove', duration: 0, x: startX + 5, y: startY + 5},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 100, x: endX + 5, y: endY + 5},
          {type: 'pointerUp', button: 0}
        ];
        await performTouchAction(actions);

        await assertScroll();
      });

      it('should swipe up menu when pointerType is mouse', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        const actions = [
          {type: 'pointerMove', duration: 0, x: startX + 5, y: startY + 5},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 100, x: endX + 5, y: endY + 5},
          {type: 'pointerUp', button: 0}
        ];
        await performAction('mouse', actions);

        await assertScroll();
      });
    });

    describe('multiple', function () {
      beforeEach(startSplitTouchActivity);
      it('should do multiple scrolls on multiple views', async function () {
        const els = await driver.elementsByClassName('android.widget.ListView');

        const actions = await B.map(els, async function (el) {
          const {height} = await el.getSize();

          const yMove = Math.round(height / 2) - 10;

          const action = [
            {type: 'pointerMove', origin: {'element-6066-11e4-a52e-4f735466cecf': el.value}},
            {type: 'pointerDown', button: 0},
            {type: 'pointerMove', origin: {'element-6066-11e4-a52e-4f735466cecf': el.value}, x: 10, y: -yMove, duration: 3000},
            {type: 'pointerUp', button: 0},
          ];
          return action;
        });

        await performTouchAction(...actions);
      });
    });

    it('should swipe on drag and drop', async function () {
      await startDragAndDropActivity();

      const el = await driver.elementById('io.appium.android.apis:id/drag_dot_1');
      const {x, y} = await el.getLocation();

      const touchActions = [
        {type: 'pointerMove', duration: 0, x: x + 30, y: y + 30},
        {type: 'pointerDown', button: 0},
        {type: 'pointerMove', duration: 100, x: x + 10, y: y + 10},
        {type: 'pointerUp', button: 0},
      ];
      await performTouchAction(touchActions);
    });
  });

  describe('touches', function () {
    let nextEl;

    beforeEach(async function () {
      await startTextSwitcherActivity();

      await driver.elementByXPath("//*[@text='0']").should.eventually.exist;
      await driver.elementByXPath("//*[@text='1']").should.eventually.be.rejectedWith(/NoSuchElement/);

      nextEl = await driver.elementByAccessibilityId('Next');
    });
    it('should touch down and up', async function () {
      const {x, y} = await nextEl.getLocation();

      const touchActions = [
        {type: 'pointerMove', duration: 100, x: x + 10, y: y + 10},
        {type: 'pointerDown', button: 0},
        {type: 'pause', duration: 3000},
        {type: 'pointerUp', button: 0},
      ];
      await performTouchAction(touchActions);

      await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
    });

    it('should touch down and up on an element by id', async function () {
      const touchActions = [
        {type: 'pointerMove', duration: 0, origin: {'element-6066-11e4-a52e-4f735466cecf': nextEl.value}},
        {type: 'pointerDown', button: 0},
        {type: 'pause', duration: 100},
        {type: 'pointerUp', button: 0},
      ];
      await performTouchAction(touchActions);

      await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
    });
  });

  describe('mjsonwp touch actions', function () {
    describe('touch', function () {
      let nextEl;

      beforeEach(async function () {
        await startTextSwitcherActivity();

        await driver.elementByXPath("//*[@text='0']").should.eventually.exist;
        await driver.elementByXPath("//*[@text='1']").should.eventually.be.rejectedWith(/NoSuchElement/);

        nextEl = await driver.elementByAccessibilityId('Next');
      });

      it('should do touch/click event', async function () {
        const {value: elementId} = nextEl;
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/click`,
          data: {
            element: elementId,
          },
        });

        await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
      });

      it('should do touch/longclick event', async function () {
        const {value: elementId} = nextEl;
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/longclick`,
          data: {
            element: elementId,
          },
        });

        await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
      });

      it('should do touch/doubleclick event', async function () {
        await driver.elementByXPath("//*[@text='2']").should.eventually.be.rejectedWith(/NoSuchElement/);

        const {value: elementId} = nextEl;
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/doubleclick`,
          data: {
            element: elementId,
          },
        });

        await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
        await driver.elementByXPath("//*[@text='2']").should.eventually.exist;
      });

      it('should touch down at a location and then touch up', async function () {
        const {x, y} = await nextEl.getLocation();

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/down`,
          data: {
            x: x + 1,
            y: y + 1,
          },
        });
        await B.delay(1000);

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/up`,
          data: {
            x: x + 1,
            y: y + 1,
          },
        });

        await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
      });
    });

    describe('move', function () {
      beforeEach(startListActivity);

      it('should touch down, move, touch up and cause a scroll event', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/down`,
          data: {
            x: startX + 1,
            y: startY + 1,
          },
        });
        await B.delay(1000);

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/move`,
          data: {
            x: endX + 1,
            y: endY + 1,
          },
        });
        await B.delay(1000);

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/up`,
          data: {
            x: endX + 1,
            y: endY + 1,
          },
        });

        await assertScroll();
      });

      it('should scroll on an element', async function () {
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/scroll`,
          data: {
            //element: el.value,
            x: 0,
            y: -300,
          },
        });
        await B.delay(1000);
      });
    });
  });

  describe('mjsonwp touch actions', function () {
    describe('multi touch actions', function () {
      // let nextEl;

      // beforeEach(async function () {
      //   await startTextSwitcherActivity();

      //   await driver.elementByXPath("//*[@text='0']").should.eventually.exist;
      //   await driver.elementByXPath("//*[@text='1']").should.eventually.be.rejectedWith(/NoSuchElement/);

      //   nextEl = await driver.elementByAccessibilityId('Next');
      // });

      // for (const method of ['tap', 'press', 'longPress']) {
      //   it(`should perform single ${method} actions on an element`, async function () {
      //     const action = new wd.TouchAction();
      //     action[method]({el: nextEl});

      //     const multiAction = new wd.MultiAction(driver);
      //     multiAction.add(action);
      //     await multiAction.perform();

      //     await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
      //   });

      //   it(`should perform single ${method} actions`, async function () {
      //     const {x, y} = await nextEl.getLocation();

      //     const action = new wd.TouchAction();
      //     action[method]({x: x + 10, y: y + 10});

      //     const multiAction = new wd.MultiAction(driver);
      //     multiAction.add(action);
      //     await multiAction.perform();

      //     await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
      //   });
      // }
    });

    describe('touch actions', function () {
      describe('tap/press/longPress', function () {
        // let nextEl;

        // beforeEach(async function () {
        //   await startTextSwitcherActivity();

        //   await driver.elementByXPath("//*[@text='0']").should.eventually.exist;
        //   await driver.elementByXPath("//*[@text='1']").should.eventually.be.rejectedWith(/NoSuchElement/);

        //   nextEl = await driver.elementByAccessibilityId('Next');
        // });

        //   for (const method of ['tap', 'press', 'longPress']) {
        //     it(`should perform single ${method} actions`, async function () {
        //       const {x, y} = await nextEl.getLocation();

        //       const action = new wd.TouchAction(driver);
        //       action[method]({x: x + 10, y: y + 10});
        //       await action.perform();

        //       await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
        //     });
        //     it(`should perform single ${method} actions on an element`, async function () {
        //       let action = new wd.TouchAction(driver);
        //       action[method]({el: nextEl});
        //       await action.perform();

        //       await driver.elementByXPath("//*[@text='1']").should.eventually.exist;
        //     });
        //   }
        // });
        // it('should perform a scroll event', async function () {
        //   await startListActivity();

        //   const {startEl, endEl} = await getScrollData();

        //   const action = new wd.TouchAction(driver);
        //   action.press({el: startEl});
        //   action.moveTo({el: endEl});
        //   action.release();
        //   await action.perform();

        //   await assertScroll();
        // });
        // it('should do multiple scrolls on multiple views', async function () {
        //   await startSplitTouchActivity();

        //   const els = await driver.elementsByClassName('android.widget.ListView');
        //   const actions = await B.map(els, async function (el) {
        //     const {height} = await el.getSize();
        //     const increment = Math.round((height / 2 - 10) / 8);

        //     let action = new wd.TouchAction()
        //       .press({element: el});
        //     for (let i = 0; i < 8; i++) {
        //       action.moveTo({element: el, x: 10, y: -i * increment});
        //     }
        //     action.release();
        //     return action;
        //   });

        //   const multiAction = new wd.MultiAction();
        //   multiAction.add(...actions);

        //   await driver.performMultiAction(multiAction);
        // });
        // it('should throw out-of-bounds when tapping coordinates outside of viewport', async function () {
        //   const {width, height} = await driver.getWindowSize();
        //   const outOfBoundsCoordinates = [
        //     [-10, 10], [10, -10], [width + 10, height - 10], [width - 10, height + 10], [width, height]
        //   ];
        //   for (let [x, y] of outOfBoundsCoordinates) {
        //     const action = new wd.TouchAction(driver);
        //     action.press({x, y});
        //     action.release();
        //     await action.perform().should.eventually.be.rejected;
        //   }
        // });
        // it('should not throw out-of-bounds exception if tapping a coordinate within viewport/', async function () {
        //   const {width, height} = await driver.getWindowSize();
        //   const inOfBoundsCoordinates = [
        //     [100, 100], [width - 100, height - 100]
        //   ];
        //   for (let [x, y] of inOfBoundsCoordinates) {
        //     const action = new wd.TouchAction(driver);
        //     action.press({x, y});
        //     action.release();
        //     await action.perform().should.eventually.be.fulfilled;
        //   }
      });
    });
  });
});
