/* eslint-disable */
"use strict";

const gulp = require('gulp');
const boilerplate = require('@appium/gulp-plugins').boilerplate.use(gulp);
const DEFAULTS = require('@appium/gulp-plugins').boilerplate.DEFAULTS;

boilerplate({
  build: 'appium-espresso-driver',
  files: DEFAULTS.files.concat('index.js'),
  testTimeout: 120000,
  e2eTest: { android: true },
  eslint: true,
});