#!/bin/bash

npx mocha --timeout 10m \
    ./test/functional/commands/mobile-e2e-specs.js \
    -g @skip-ci -i --exit
