---
title: Backdoor Method
---

The Espresso driver allows to directly invoke a method from the application under test using the
[`mobile: backdoor`](../reference/execute-methods.md#mobile-backdoor) execute method.

!!! info

    Only 'public' methods can be invoked (they must have the Kotlin `open` modifier).

## Targets

The execute method allows to specify the application methods using the `methods` parameter, and
distinguish the target type depending on the value of the `target` parameter:

* `application`: `methods` will be invoked on the application class
* `activity`: `methods` will be invoked on the current application activity
* `element`: `methods` will be invoked on the selected view element

## Argument Types

Arguments provided in `methods` must have one of the following primitive types: `int`, `boolean`,
`byte`, `short`, `long`, `float`, or `char`.

The driver also supports object wrappers over primitive types with the `java.lang.*` fully
qualified name: `java.lang.CharSequence`, `java.lang.String`, `java.lang.Integer`,
`java.lang.Float`, `java.lang.Double`, `java.lang.Boolean`, `java.lang.Long`, `java.lang.Short`,
`java.lang.Character`, etc.

## Example

```json
{
   "target": "activity",
   "methods":
   [
     {
       "name": "firstMethod",
     },
     {
       "name": "secondMethod",
       "args": [
         {"value": "foo", "type": "java.lang.CharSequence"},
         {"value": 1, "type": "int"}
       ]
     }
   ]
}
```

The flow in the above example is as follows:

1. `firstMethod` is called on the current activity instance. It takes no arguments.
2. `secondMethod` is called on the object returned by `firstMethod`. It accepts two arguments, with the first one being `foo` of type `java.lang.CharSequence`, and the second being `1` of type `int`.
3. The result of `secondMethod` is returned to the client.
