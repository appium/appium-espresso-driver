import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import wd from 'wd';
import request from 'request-promise';
import B from 'bluebird';
import { HOST, PORT, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';
import { startServer } from '../../..';


chai.should();
chai.use(chaiAsPromised);

// TODO: Add missing client features to admc/wd

describe('elementByXPath', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let server;
  before(async function () {
    server = await startServer(PORT, HOST);
    driver = wd.promiseChainRemote(HOST, PORT);
  });
  after(async function () {
    try {
      await server.close();
    } catch (ign) {}
  });
  beforeEach(async function () {
    try {
      await driver.init(APIDEMO_CAPS);
    } catch (ign) {}
  });
  afterEach(async function () {
    try {
      await driver.quit();
    } catch (ign) {}
  });

  let idCounter = 0;

  const performTouchAction = async function (...actionsArrays) {
    const actionsRoot = [];

    for (let actions of actionsArrays) {
      actionsRoot.push({
        type: 'pointer',
        id: `id_${idCounter++}`,
        parameters: {
          pointerType: 'touch'
        },
        actions,
      });
    }

    let sessionId = await driver.getSessionId();
    const options = {
      method: 'POST',
      uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/actions`,
      body: {actions: actionsRoot},
      json: true,
    };
    return request(options);
  };

  describe('fingerpaint', async function () {
    it('should draw the letter L on fingerpaint', async function () {
      await (await driver.elementByAccessibilityId("Graphics")).click();
      await (await driver.elementByAccessibilityId("FingerPaint")).click();
      let canvas = await driver.elementById("android:id/content");
      let {x, y} = await canvas.getLocation();
      const touchActions = [
        {"type": "pointerMove", duration: 100, x: x + 10, y: y + 10},
        {"type": "pointerDown", "button": 0},
        {"type": "pointerMove", duration: 100,  x: x + 10, y: y + 1000},
        {"type": "pause", duration: 100},
        {"type": "pointerMove", duration: 100,  x: x + 1000, y: y + 1000},
        {"type": "pointerCancel", "button": 0},
      ];
      await performTouchAction(touchActions);
    });

    it('should draw two parallel lines on fingerpaint', async function () {
      await (await driver.elementByAccessibilityId("Graphics")).click();
      await (await driver.elementByAccessibilityId("FingerPaint")).click();
      let canvas = await driver.elementById("android:id/content");
      let {x, y} = await canvas.getLocation();
      const touchActionOne = [
        {"type": "pointerMove", x: x + 10, y: y + 10},
        {"type": "pointerDown", "button": 0},
        {"type": "pointerMove", duration: 2000,  x: x + 10, y: y + 1000},
        {"type": "pointerUp", "button": 0},
      ];

      const touchActionTwo = [
        {"type": "pointerMove", x: x + 200, y: y + 10},
        {"type": "pointerDown", "button": 0},
        {"type": "pointerMove", duration: 2000,  x: x + 200, y: y + 1000},
        {"type": "pointerUp", "button": 0},
      ];
      await performTouchAction(touchActionOne, touchActionTwo);
    });
  });

  describe('scrolling/swiping', async function () {
    it.skip('should scroll up menu', async function () {
      await (await driver.elementByAccessibilityId("Views")).click();
      let gcEl = await driver.elementByAccessibilityId("Game Controller Input");
      let {x, y} = await gcEl.getLocation();
      let customEl = await driver.elementByAccessibilityId("Custom");
      let {x:xEnd, y:yEnd} = await customEl.getLocation();

      const actions = [
        {"type": "pointerMove", "duration": 0, x: x + 5, y: y + 5},
        {"type": "pointerDown", "button": 0},
        {"type": "pointerMove", "duration": 200,  x: xEnd + 5, y: yEnd + 5},
        {"type": "pointerUp", "button": 0}
      ];
      await performTouchAction(actions);
      await driver.elementByXPath("//*[@text='ImageButton']");
    });

    it.skip('should swipe up menu', async function () {
      await (await driver.elementByAccessibilityId("Views")).click();
      let gcEl = await driver.elementByAccessibilityId("Game Controller Input");
      let {x, y} = await gcEl.getLocation();
      let customEl = await driver.elementByAccessibilityId("Custom");
      let {x:xEnd, y:yEnd} = await customEl.getLocation();

      const actions = [
        {"type": "pointerMove", "duration": 0,  x: x + 5, y: y + 5},
        {"type": "pointerDown", "button": 0},
        {"type": "pointerMove", "duration": 100,  x: xEnd + 5, y: yEnd + 5},
        {"type": "pointerUp", "button": 0}
      ];
      await performTouchAction(actions);
      await driver.elementByXPath("//*[@text='ImageButton']");
    });
  });

  describe('touches', async function () {
    it('should swipe on drag and drop', async function () {
      await (await driver.elementByAccessibilityId("Views")).click();
      await (await driver.elementByAccessibilityId("Drag and Drop")).click();
      let el = await driver.elementById("io.appium.android.apis:id/drag_dot_1");
      let {x, y} = await el.getLocation();
      const touchActions = [
        {"type": "pointerMove", "duration": 0, x: x + 30, y: y + 30},
        {"type": "pointerDown", "button": 0},
        {"type": "pointerMove", "duration": 100,  x: x + 10, y: y + 10},
        {"type": "pointerUp", "button": 0}
      ];
      await performTouchAction(touchActions);
    });

    it('should touch down and up', async function () {
      const el = await driver.elementByAccessibilityId("Accessibility");
      let {x, y} = await el.getLocation();
      const touchActions = [
        {"type": "pointerMove", "duration": 0, x: x + 10, y: y + 10},
        {"type": "pointerDown", "button": 0},
        {"type": "pause", "duration": 100},
        {"type": "pointerUp", "button": 0},
      ];
      await performTouchAction(touchActions);
      await driver.elementByAccessibilityId("Accessibility Node Provider").should.eventually.exist;
    });

    it('should touch down and up on an element by id', async function () {
      const el = await driver.elementByAccessibilityId("Accessibility");
      const touchActions = [
        {"type": "pointerMove", "duration": 0, "origin": {"element-6066-11e4-a52e-4f735466cecf": el.value}},
        {"type": "pointerDown", "button": 0},
        {"type": "pause", "duration": 100},
        {"type": "pointerUp", "button": 0},
      ];
      await performTouchAction(touchActions);
      await driver.elementByAccessibilityId("Accessibility Node Provider").should.eventually.exist;

      // Check that it throws a stale element exception if we try again on the same element after it has become stale
      await performTouchAction(touchActions).should.eventually.be.rejectedWith(/no longer exists /);
    });

  });

  describe('mjsonwp touch actions', function () {
    let sessionId;

    it('should do touch/click event', async function () {
      let el = await driver.elementByAccessibilityId("Accessibility");
      sessionId = await driver.getSessionId();
      const {value:elementId} = el;
      const options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/click`,
        body: {
          element: elementId,
        },
        json: true,
      };
      await request(options);
      await driver.elementByAccessibilityId('Accessibility Node Provider').should.eventually.exist;
    });

    it('should do touch/longclick event', async function () {
      let el = await driver.elementByAccessibilityId("Accessibility");
      sessionId = await driver.getSessionId();
      const {value:elementId} = el;
      const options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/longclick`,
        body: {
          element: elementId,
        },
        json: true,
      };
      await request(options);
      await driver.elementByAccessibilityId('Accessibility Node Provider').should.eventually.exist;
    });

    it('should do touch/doubleclick event', async function () {
      let el = await driver.elementByAccessibilityId("Accessibility");
      sessionId = await driver.getSessionId();
      const {value:elementId} = el;
      const options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/doubleclick`,
        body: {
          element: elementId,
        },
        json: true,
      };
      await request(options);
    });

    it('should touch down at a location and then touch up', async function () {
      sessionId = await driver.getSessionId();
      let el = await driver.elementByAccessibilityId("Animation");
      let {x, y} = await el.getLocation();
      const options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/down`,
        body: {
          x: x + 1,
          y: y + 1,
        },
        json: true,
      };
      await request(options);
      await B.delay(1000);
      const optionsTwo = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/up`,
        body: {
          x: x + 1,
          y: y + 1,
        },
        json: true,
      };
      await request(optionsTwo);
      await driver.elementByAccessibilityId("Bouncing Balls").should.eventually.exist;
    });

    it('should touch down, move, touch up and cause a scroll event', async function () {
      await (await driver.elementByAccessibilityId("Views")).click();

      sessionId = await driver.getSessionId();
      let el = await driver.elementByAccessibilityId("Gallery");
      let {x:startX, y:startY} = await el.getLocation();
      el = await driver.elementByAccessibilityId("Controls");
      let {x:endX, y:endY} = await el.getLocation();
      let options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/down`,
        body: {
          x: startX + 1,
          y: startY + 1,
        },
        json: true,
      };
      await request(options);
      await B.delay(1000);

      options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/move`,
        body: {
          x: endX + 1,
          y: endY + 1,
        },
        json: true,
      };
      await request(options);
      await B.delay(1000);

      options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/up`,
        body: {
          x: endX + 1,
          y: endY + 1,
        },
        json: true,
      };
      await request(options);
      await driver.elementByAccessibilityId("Hover Events").should.eventually.exist;

    });

    it('should scroll on an element', async function () {
      await (await driver.elementByAccessibilityId("Views")).click();

      sessionId = await driver.getSessionId();
      let options = {
        method: 'POST',
        uri: `http://${HOST}:${PORT}/wd/hub/session/${sessionId}/touch/scroll`,
        body: {
          //element: el.value,
          x: 0,
          y: -300,
        },
        json: true,
      };
      await request(options);
      await B.delay(1000);
    });
  });
});
