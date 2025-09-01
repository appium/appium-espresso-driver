import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';
import { AssertionError } from 'assert';


describe('clipboard', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  let chai;

  before(async function () {
    chai = await import('chai');
    const chaiAsPromised = await import('chai-as-promised');

    chai.should();
    chai.use(chaiAsPromised.default);

    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get clipboard', async function () {
    await driver.execute('mobile: setClipboard', {
      content: new Buffer.from('Hello').toString('base64'), contentType: 'plaintext'
    });
    // 'SGVsbG8=' is 'Hello' in base 64 encoding with a new line.
    const text = await driver.execute('mobile:getClipboard');
    try {
      text.should.eql('SGVsbG8=');
    } catch (e) {
      if (e instanceof AssertionError) {
        // API level 23 and 25 emulator has '\n'
        text.should.eql('SGVsbG8=\n');
      } else {
        throw e;
      }
    }
    (Buffer.from(text, 'base64').toString()).should.eql('Hello');
  });
});
