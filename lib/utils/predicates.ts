/** Returns true when value is a plain object. */
export function isPlainObject(value: unknown): value is Record<string, unknown> {
  if (value === null || typeof value !== 'object') {
    return false;
  }
  const prototype = Object.getPrototypeOf(value);
  return prototype === null || prototype === Object.prototype;
}

/** Generic emptiness check for nullish, strings, arrays, and plain objects. */
export function isEmptyValue(value: unknown): boolean {
  if (value == null) {
    return true;
  }
  if (typeof value === 'string' || Array.isArray(value) || Buffer.isBuffer(value)) {
    return value.length === 0;
  }
  if (value instanceof Map || value instanceof Set) {
    return value.size === 0;
  }
  if (typeof value === 'object' || typeof value === 'function') {
    return Object.keys(value).length === 0;
  }
  return true;
}

/** Escapes RegExp special characters in a string. */
export function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
