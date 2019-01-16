import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
/*import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';*/

chai.should();
chai.use(chaiAsPromised);

describe('mobile web atoms', function () {
  //this.timeout(MOCHA_TIMEOUT);

  //let driver;
  before(async function () {
    // TODO: Make these 'WebView' caps
    //driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    //await deleteSession();
  });

  it('should run web atoms on a WebView', async function () {
    // TODO: Do a kitchen sink example of a WebView having web atoms run on it
  });

});
