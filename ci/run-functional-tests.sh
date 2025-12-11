#!/bin/bash

npx mocha --timeout 10m \
    ./test/functional/commands/jetpack-compose-source-e2e-specs.ts \
    ./test/functional/commands/element-values-e2e-specs.ts \
    ./test/functional/commands/contexts-e2e-specs.ts \
    ./test/functional/commands/element-values-e2e-specs.ts \
    ./test/functional/commands/find-e2e-specs.ts \
    ./test/functional/commands/jetpack-componse-element-values-e2e-specs.ts \
    ./test/functional/commands/jetpack-compose-attributes-e2e-specs.ts \
    ./test/functional/commands/jetpack-compose-e2e-specs.ts \
    ./test/functional/commands/keyboard-e2e-specs.ts \
    ./test/functional/commands/attributes-e2e-specs.ts \
    ./test/functional/commands/orientation-e2e-specs.ts \
    ./test/functional/commands/size-e2e-specs.ts \
    ./test/functional/commands/source-e2e-specs.ts \
    ./test/functional/commands/mobile-e2e-specs.ts \
    ./test/functional/webview/web-e2e-specs.ts \
    ./test/functional/webview/webatoms-e2e-specs.ts \
    ./test/functional/*-specs.ts \
    -g @skip-ci -i --exit
