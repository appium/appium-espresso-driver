/* eslint-disable */
"use strict";

var gulp = require('gulp'),
    boilerplate = require('appium-gulp-plugins').boilerplate.use(gulp);

boilerplate({
  build: 'appium-espresso-driver',
  jscs: false,
  testTimeout: 120000,
  e2eTest: { android: true },
  eslint: true,
});
