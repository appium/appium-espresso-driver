---
title: Settings
---

The Espresso driver exposes various settings through Appium's [Settings API](https://appium.io/docs/en/latest/guides/settings/).

## `driver`

| Type | Default |
| -- | -- |
| `string` | `espresso` |

The name of the subdriver to use for interaction with elements. Supported values are `espresso` and
`compose`.

Switching the value to `compose` enables interactions with [Jetpack Compose](https://developer.android.com/compose)-based
application user interfaces. Refer to the [Jetpack Compose guide](../guides/compose.md) for more
details.
