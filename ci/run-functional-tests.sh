#!/bin/bash

npx mocha --timeout 10m \
    ./test/functional/commands/clipboard-e2e-specs.js \
    ./test/functional/commands/contexts-e2e-specs.js \
    ./test/functional/commands/element-values-e2e-specs.js \
    -g @skip-ci -i --exit

    # ./test/functional/*-specs.js \
    # ./test/functional/commands/attributes-e2e-specs.js \
    # ./test/functional/commands/jetpack-compose-attributes-e2e-specs.js \
