## The Espresso Driver for Android

### Server Arguments

Appium 2.0 Usage: `node . --driver-args='{"espresso": {[argName]: [argValue]}}'`

<expand_table>

|Argument|Default|Description|Example|
|----|-------|-----------|-------|

|`"reboot"`|false|reboot emulator after each session and kill it at the end|`--driver-args='{"espresso": {"reboot": true}}'`|
|`"suppressKillServer"`|false| If set, prevents Appium from killing the adb server instance|`--driver-args='{"espresso": {"suppressKillServer": true}}'`|