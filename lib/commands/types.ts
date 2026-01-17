/**
 * A dictionary where each key contains a unique display identifier
 * and values are dictionaries with following items:
 * - id: Display identifier
 * - name: Display name, could be empty
 * - isDefault: Whether this display is the default one
 * - payload: The actual PNG screenshot data encoded to base64 string
 */
export interface ScreenshotsInfo {
  [displayId: string]: {
    id: string;
    name: string;
    isDefault: boolean;
    payload: string;
  };
}
