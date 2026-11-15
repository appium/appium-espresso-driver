import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {describe, it} from 'node:test';

import {HINTS, findHints, withHints} from '../../lib/commands/server/hints.js';

const DOCS_PAGE = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  '../../../docs/troubleshooting/class-conflicts.md',
);

describe('hints', function () {
  it('should recognize the relocation failures', function () {
    const crash =
      'java.lang.IllegalAccessError: Method ' +
      "'void io.appium.espressoserver.shaded.androidx.test.runner.Foo.bar()' is inaccessible to class " +
      'androidx.test.runner.AndroidJUnitRunner';
    assert.match(findHints(crash)[0], /shadeServer/);
    assert.match(findHints('IllegalStateException: ViewTreeLifecycleOwner not found from ComposeView')[0], /shadeServer/);
    assert.deepEqual(findHints('The ViewTreeLifecycleOwner is set'), []);
    assert.match(findHints('Exception handler was not found via a ServiceLoader')[0], /shadeServer/);
    assert.match(
      findHints(
        'androidx.startup.StartupException: java.lang.ClassCastException: ' +
          'io.appium.Foo cannot be cast to androidx.startup.Initializer',
      )[0],
      /shadeServer/,
    );
  });

  it('should recognize the Compose version mismatch', function () {
    const [hint] = findHints(
      'java.lang.NoSuchMethodError: No static method foo() in class Landroidx/compose/ui/Foo; (androidx.compose.ui.Foo)',
    );
    assert.match(hint, /composeVersion/);
  });

  it('should not find hints for unrelated failures', function () {
    assert.deepEqual(findHints('java.lang.NullPointerException at io.appium.Foo'), []);
  });

  it('should not duplicate hints', function () {
    assert.equal(findHints('Exception handler was not found via a ServiceLoader\nException handler was not found via a ServiceLoader').length, 1);
  });

  it('should append the hints to the message', function () {
    assert.equal(withHints('message', 'nothing'), 'message');
    const result = withHints('message', 'Exception handler was not found via a ServiceLoader');
    assert.ok(result.startsWith('message\nHint: '));
    assert.ok(result.includes('/troubleshooting/class-conflicts/#service-loader'));
  });

  it('should have a row in the docs for every hint', async function () {
    const docs = await readFile(DOCS_PAGE, 'utf8');
    for (const {anchor} of HINTS) {
      assert.ok(docs.includes(`<a id="${anchor}"></a>`), `${anchor} is not documented`);
    }
    const anchors = HINTS.map(({anchor}) => anchor);
    assert.equal(new Set(anchors).size, anchors.length);
  });
});
