import axios from 'axios';
import B from 'bluebird';
import _ from 'lodash';
import chai, {expect} from 'chai';
import chaiAsPromised from 'chai-as-promised';
import {initSession, deleteSession, HOST, PORT, MOCHA_TIMEOUT} from '../helpers/session';
import {APIDEMO_CAPS} from '../desired';

chai.use(chaiAsPromised);

describe('touch actions -', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver: any;
  let sessionId: string;

  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
    sessionId = await driver.sessionId;
  });
  after(async function () {
    await deleteSession();
  });

  async function startListActivity() {
    await driver.execute('mobile:startActivity', {
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.List5',
    });
  }

  async function startFingerPaintActivity() {
    await driver.execute('mobile:startActivity', {
      appPackage: 'io.appium.android.apis',
      appActivity: '.graphics.FingerPaint',
    });
  }

  async function startSplitTouchActivity() {
    await driver.execute('mobile:startActivity', {
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.SplitTouchView',
    });
  }

  async function startDragAndDropActivity() {
    await driver.execute('mobile:startActivity', {
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.DragAndDropDemo',
    });
  }

  async function startTextSwitcherActivity() {
    await driver.execute('mobile:startActivity', {
      appPackage: 'io.appium.android.apis',
      appActivity: '.view.TextSwitcher1',
    });
  }

  async function getScrollData() {
    const els = (await driver.$$(
      await driver.findElements('class name', 'android.widget.TextView'),
    )) as unknown as any[];

    // the last element is the title of the view, and often the
    // second-to-last is actually off the screen
    const startEl = els[els.length - 3];
    const {x: startX, y: startY} = await startEl.getLocation();

    const endEl = _.first(els)!;
    const {x: endX, y: endY} = await endEl.getLocation();

    return {startX, startY, endX, endY, startEl, endEl};
  }

  let idCounter = 0;

  const performAction = async function (pointerType: string, ...actionsArrays: any[]) {
    const actionsRoot: any[] = [];

    for (const actions of actionsArrays) {
      actionsRoot.push({
        type: 'pointer',
        id: `id_${idCounter++}`,
        parameters: {
          pointerType,
        },
        actions,
      });
    }

    await axios({
      method: 'POST',
      url: `http://${HOST}:${PORT}/session/${sessionId}/actions`,
      data: {actions: actionsRoot},
    });
  };

  const performTouchAction = async function (...actionsArrays: any[]) {
    return await performAction('touch', ...actionsArrays);
  };

  describe('fingerpaint', function () {
    beforeEach(startFingerPaintActivity);

    it('should draw the letter L on fingerpaint', async function () {
      const canvas = await driver.$(await driver.findElement('id', 'android:id/content'));
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
      await performTouchAction(touchActions as any);
    });

    it('should draw two parallel lines on fingerpaint', async function () {
      const canvas = await driver.$(await driver.findElement('id', 'android:id/content'));
      const {x, y} = await canvas.getLocation();
      const {height, width} = await canvas.getSize();

      const touchActions = [10, width - 10].reduce<any[]>(function (touchActions, xOffset) {
        touchActions.push([
          {type: 'pointerMove', x: x + xOffset, y: y + 10},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 2000, x: x + xOffset, y: y + Math.round(height / 2)},
          {type: 'pointerUp', button: 0},
        ]);
        return touchActions;
      }, []);
      await performTouchAction(...(touchActions as any));
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
          {type: 'pointerUp', button: 0},
        ];
        await performTouchAction(actions as any);
      });

      it('should swipe up menu', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        const actions = [
          {type: 'pointerMove', duration: 0, x: startX + 5, y: startY + 5},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 100, x: endX + 5, y: endY + 5},
          {type: 'pointerUp', button: 0},
        ];
        await performTouchAction(actions as any);
      });

      it('should swipe up menu when pointerType is mouse', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        const actions = [
          {type: 'pointerMove', duration: 0, x: startX + 5, y: startY + 5},
          {type: 'pointerDown', button: 0},
          {type: 'pointerMove', duration: 100, x: endX + 5, y: endY + 5},
          {type: 'pointerUp', button: 0},
        ];
        await performAction('mouse', actions);
      });
    });

    describe('multiple', function () {
      beforeEach(startSplitTouchActivity);
      it('should do multiple scrolls on multiple views', async function () {
        const els = (await driver.$$(
          await driver.findElements('class name', 'android.widget.ListView'),
        )) as unknown as any[];

        const actions = await B.map(els, async function (el: any) {
          const {height} = await el.getSize();

          const yMove = Math.round(height / 2) - 10;

          const action = [
            {type: 'pointerMove', origin: {'element-6066-11e4-a52e-4f735466cecf': el.elementId}},
            {type: 'pointerDown', button: 0},
            {
              type: 'pointerMove',
              origin: {'element-6066-11e4-a52e-4f735466cecf': el.elementId},
              x: 10,
              y: -yMove,
              duration: 3000,
            },
            {type: 'pointerUp', button: 0},
          ];
          return action;
        });

        await performTouchAction(...(actions as any));
      });
    });

    it('should swipe on drag and drop', async function () {
      await startDragAndDropActivity();

      const el = await driver.$(
        await driver.findElement('id', 'io.appium.android.apis:id/drag_dot_1'),
      );
      const {x, y} = await el.getLocation();

      const touchActions = [
        {type: 'pointerMove', duration: 0, x: x + 30, y: y + 30},
        {type: 'pointerDown', button: 0},
        {type: 'pointerMove', duration: 100, x: x + 10, y: y + 10},
        {type: 'pointerUp', button: 0},
      ];
      await performTouchAction(touchActions as any);
    });
  });

  describe('touches', function () {
    let nextEl;

    beforeEach(async function () {
      await startTextSwitcherActivity();

      expect(await driver.$("//*[@text='0']")).to.exist;

      nextEl = await driver.$('~Next');
    });
    it('should touch down and up', async function () {
      const {x, y} = await nextEl.getLocation();

      const touchActions = [
        {type: 'pointerMove', duration: 100, x: x + 10, y: y + 10},
        {type: 'pointerDown', button: 0},
        {type: 'pause', duration: 3000},
        {type: 'pointerUp', button: 0},
      ];
      await performTouchAction(touchActions as any);

      expect(await driver.$("//*[@text='1']")).to.exist;
    });

    it('should touch down and up on an element by id', async function () {
      const touchActions = [
        {
          type: 'pointerMove',
          duration: 0,
          origin: {'element-6066-11e4-a52e-4f735466cecf': nextEl.elementId},
        },
        {type: 'pointerDown', button: 0},
        {type: 'pause', duration: 100},
        {type: 'pointerUp', button: 0},
      ];
      await performTouchAction(touchActions);

      expect(await driver.$("//*[@text='1']")).to.exist;
    });
  });

  describe.skip('mjsonwp touch actions', function () {
    describe('touch', function () {
      let nextEl;

      beforeEach(async function () {
        await startTextSwitcherActivity();

        expect(await driver.$("//*[@text='0']")).to.exist;
        await expect(driver.$("//*[@text='1']")).to.be.rejectedWith(/NoSuchElement/);

        nextEl = await driver.elementByAccessibilityId('Next');
      });

      it('should do touch/click event', async function () {
        const {value: elementId} = nextEl;
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/click`,
          data: {
            element: elementId,
          },
        });

        expect(await driver.$("//*[@text='1']")).to.exist;
      });

      it('should do touch/longclick event', async function () {
        const {value: elementId} = nextEl;
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/longclick`,
          data: {
            element: elementId,
          },
        });

        expect(await driver.$("//*[@text='1']")).to.exist;
      });

      it('should do touch/doubleclick event', async function () {
        await expect(driver.$("//*[@text='2']")).to.be.rejectedWith(/NoSuchElement/);

        const {value: elementId} = nextEl;
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/doubleclick`,
          data: {
            element: elementId,
          },
        });

        expect(await driver.$("//*[@text='1']")).to.exist;
        expect(await driver.$("//*[@text='2']")).to.exist;
      });

      it('should touch down at a location and then touch up', async function () {
        const {x, y} = await nextEl.getLocation();

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/down`,
          data: {
            x: x + 1,
            y: y + 1,
          },
        });
        await B.delay(1000);

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/up`,
          data: {
            x: x + 1,
            y: y + 1,
          },
        });

        expect(await driver.$("//*[@text='1']")).to.exist;
      });
    });

    // No longer exists.
    describe.skip('move', function () {
      beforeEach(startListActivity);

      it('should touch down, move, touch up and cause a scroll event', async function () {
        const {startX, startY, endX, endY} = await getScrollData();

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/down`,
          data: {
            x: startX + 1,
            y: startY + 1,
          },
        });
        await B.delay(1000);

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/move`,
          data: {
            x: endX + 1,
            y: endY + 1,
          },
        });
        await B.delay(1000);

        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/up`,
          data: {
            x: endX + 1,
            y: endY + 1,
          },
        });
      });

      it('should scroll on an element', async function () {
        await axios({
          method: 'POST',
          url: `http://${HOST}:${PORT}/session/${sessionId}/touch/scroll`,
          data: {
            //element: el.elementId,
            x: 0,
            y: -300,
          },
        });
        await B.delay(1000);
      });
    });
  });
});
