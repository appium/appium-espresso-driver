import chai from 'chai';
import chaiAsPromised from 'chai-as-promised';
import { initSession, deleteSession, MOCHA_TIMEOUT } from '../helpers/session';
import { APIDEMO_CAPS } from '../desired';


chai.should();
chai.use(chaiAsPromised);


describe('clipboard', function () {
  this.timeout(MOCHA_TIMEOUT);

  let driver;
  before(async function () {
    driver = await initSession(APIDEMO_CAPS);
  });
  after(async function () {
    await deleteSession();
  });

  it('should set and get clipboard', async function () {
    await driver.setClipboard(new Buffer.from('Hello').toString('base64'), 'plaintext');
    // 'SGVsbG8=' is 'Hello' in base 64 encoding with a new line.
    const text = await driver.getClipboard('PLAINTEXT');
    text.should.eql('SGVsbG8=\n');
    (new Buffer('SGVsbG8=\n', 'base64').toString()).should.eql('Hello');
  });
});
