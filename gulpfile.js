/* eslint-disable */
"use strict";

const gulp = require('gulp');
const boilerplate = require('appium-gulp-plugins').boilerplate.use(gulp);

boilerplate({
  build: 'appium-espresso-driver',
  testTimeout: 120000,
  e2eTest: { android: true },
  eslint: true,
});
