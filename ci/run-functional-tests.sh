#!/bin/bash

npx mocha --timeout 10m \
    ./test/functional/commands/element-values-e2e-specs.js \
    ./test/functional/commands/contexts-e2e-specs.js \
    ./test/functional/commands/element-values-e2e-specs.js \
    ./test/functional/commands/find-e2e-specs.js \
    ./test/functional/commands/jetpack-componse-element-values-e2e-specs.js \
    ./test/functional/commands/jetpack-compose-attributes-e2e-specs.js \
    ./test/functional/commands/jetpack-compose-e2e-specs.js \
    ./test/functional/*-specs.js \
    ./test/functional/commands/attributes-e2e-specs.js \
    -g @skip-ci -i --exit
