#!/bin/bash
set -e

npm run pree2e-test

node --test --test-concurrency=1 --test-timeout=600000 --test-skip-pattern @skip-ci \
    ./build/test/functional/commands/jetpack-compose-source-e2e-specs.js \
    ./build/test/functional/commands/element-values-e2e-specs.js \
    ./build/test/functional/commands/contexts-e2e-specs.js \
    ./build/test/functional/commands/find-e2e-specs.js \
    ./build/test/functional/commands/jetpack-componse-element-values-e2e-specs.js \
    ./build/test/functional/commands/jetpack-compose-attributes-e2e-specs.js \
    ./build/test/functional/commands/jetpack-compose-e2e-specs.js \
    ./build/test/functional/commands/keyboard-e2e-specs.js \
    ./build/test/functional/commands/attributes-e2e-specs.js \
    ./build/test/functional/commands/orientation-e2e-specs.js \
    ./build/test/functional/commands/size-e2e-specs.js \
    ./build/test/functional/commands/source-e2e-specs.js \
    ./build/test/functional/commands/mobile-e2e-specs.js \
    ./build/test/functional/webview/web-e2e-specs.js \
    ./build/test/functional/webview/webatoms-e2e-specs.js \
    ./build/test/functional/*-specs.js
