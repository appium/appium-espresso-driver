import type {DriverCaps, DriverOpts, W3CDriverCaps} from '@appium/types';
import type {EspressoConstraints} from './constraints';

export type EspressoDriverOpts = DriverOpts<EspressoConstraints>;

export type W3CEspressoDriverCaps = W3CDriverCaps<EspressoConstraints>;

export interface DeviceInfo {
  apiVersion: string;
  platformVersion: string;
  manufacturer: string;
  model: string;
  realDisplaySize: string;
  displayDensity: number;
}

export interface DeviceInfoCaps {
  deviceApiLevel: number;
  platformVersion: string;
  deviceScreenSize: string;
  deviceModel: string;
  deviceManufacturer: string;
  deviceScreenDensity: number;
  deviceUDID: string;
}

export type EspressoDriverCaps = DriverCaps<EspressoConstraints> & DeviceInfoCaps;
