import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


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
    await driver.setClipboard(new Buffer.from('Hello').toString('base64'), 'plaintext');
    // 'SGVsbG8=' is 'Hello' in base 64 encoding with a new line.
    const text = await driver.getClipboard('PLAINTEXT');
    try {
      text.should.eql('SGVsbG8=');
    } catch (AssertionError) {
      // API level 23 and 25 emulator has '\n'
      text.should.eql('SGVsbG8=\n');
    }
    (Buffer.from(text, 'base64').toString()).should.eql('Hello');
  });
});
