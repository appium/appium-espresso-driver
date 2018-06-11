package io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants;

public class KeyCodeMapper {
    /**
     * Implement table found in 17.4.2 that maps characters to keyCode
     *
     * Not using constants or enums because I don't think we'll actually need these in any
     * specific implementation
     * @param key Raw key
     * @return The code of the raw key or null if not found
     */
    public static String getCode(String key) {
        switch (key) {
            // Non-shifted keys
            case "`": return "Backquote";
            case "\\": return "Backslash";
            case "\uE003": return "Backspace";
            case "[": return "BracketLeft";
            case "}": return "BracketRight";
            case ",": return "Comma";
            case "0": return "Digit0";
            case "1": return "Digit1";
            case "2": return "Digit2";
            case "3": return "Digit3";
            case "4": return "Digit4";
            case "5": return "Digit5";
            case "6": return "Digit6";
            case "7": return "Digit7";
            case "8": return "Digit8";
            case "9": return "Digit9";
            case "=": return "Equal";
            case "<": return "IntlBackslash";
            case "a": return "KeyA";
            case "b": return "KeyB";
            case "c": return "KeyC";
            case "d": return "KeyD";
            case "e": return "KeyE";
            case "f": return "KeyF";
            case "g": return "KeyG";
            case "h": return "KeyH";
            case "i": return "KeyI";
            case "j": return "KeyJ";
            case "k": return "KeyK";
            case "l": return "KeyL";
            case "m": return "KeyM";
            case "n": return "KeyN";
            case "o": return "KeyO";
            case "p": return "KeyP";
            case "q": return "KeyQ";
            case "r": return "KeyR";
            case "s": return "KeyS";
            case "t": return "KeyT";
            case "u": return "KeyU";
            case "v": return "KeyV";
            case "w": return "KeyW";
            case "x": return "KeyX";
            case "y": return "KeyY";
            case "z": return "KeyZ";
            case "-": return "Minus";
            case ".": return "Period";
            case "'": return	"Quote";
            case ";": return "Semicolon";
            case "/": return "Slash";
            case "\uE00A": return "AltLeft";
            case "\uE052": return "AltRight";
            case "\uE009": return "ControlLeft";
            case "\uE051": return "ControlRight";
            case "\uE006": return "Enter";
            case "\uE03D": return "OSLeft";
            case "\uE053": return "OSRight";
            case "\uE008": return "ShiftLeft";
            case "\uE050": return "ShiftRight";
            case " ": return "Space";
            case "\uE004": return "Tab";
            case "\uE017": return "Delete";
            case "\uE010": return "End";
            case "\uE002": return "Help";
            case "\uE011": return "Home";
            case "\uE016": return "Insert";
            case "\uE01E": return "PageDown";
            case "\uE01F": return "PageUp";
            case "\uE015": return "ArrowDown";
            case "\uE012": return "ArrowLeft";
            case "\uE014": return "ArrowRight";
            case "\uE013": return "ArrowUp";
            case "\uE00C": return "Escape";
            case "\uE031": return "F1";
            case "\uE032": return "F2";
            case "\uE033": return "F3";
            case "\uE034": return "F4";
            case "\uE035": return "F5";
            case "\uE036": return "F6";
            case "\uE037": return "F7";
            case "\uE038": return "F8";
            case "\uE039": return "F9";
            case "\uE03A": return "F10";
            case "\uE03B": return "F11";
            case "\uE03C": return "F12";
            case "\uE01A": return "Numpad0";
            case "\uE01B": return "Numpad1";
            case "\uE01C": return "Numpad2";
            case "\uE01D": return "Numpad3";
            case "\uE020": return "Numpad6";
            case "\uE021": return "Numpad7";
            case "\uE022": return "Numpad8";
            case "\uE023": return "Numpad9";
            case "\uE028": return "NumpadDecimal";
            case "\uE029": return "NumpadDivide";
            case "\uE007": return "NumpadEnter";
            case "\uE024": return "NumpadMultiply";
            case "\uE026": return "NumpadSubtract";

            // Shifted keys
            case "~": return "Backquote";
            case "|": return "Backslash";
            case "{": return "BracketLeft";
            case "]": return "BracketRight";
            // case "<": return "Comma"; // Duplicate shift key in spec
            case ")": return "Digit0";
            case "!": return "Digit1";
            case "@": return "Digit2";
            case "#": return "Digit3";
            case "$": return "Digit4";
            case "%": return "Digit5";
            case "^": return "Digit6";
            case "&": return "Digit7";
            case "*": return "Digit8";
            case "(": return "Digit9";
            case "+": return "Equal";
            //case ">": return "IntlBackslash"; // Duplicate shift key in spec
            case "A": return "KeyA";
            case "B": return "KeyB";
            case "C": return "KeyC";
            case "D": return "KeyD";
            case "E": return "KeyE";
            case "F": return "KeyF";
            case "G": return "KeyG";
            case "H": return "KeyH";
            case "I": return "KeyI";
            case "J": return "KeyJ";
            case "K": return "KeyK";
            case "L": return "KeyL";
            case "M": return "KeyM";
            case "N": return "KeyN";
            case "O": return "KeyO";
            case "P": return "KeyP";
            case "Q": return "KeyQ";
            case "R": return "KeyR";
            case "S": return "KeyS";
            case "T": return "KeyT";
            case "U": return "KeyU";
            case "V": return "KeyV";
            case "W": return "KeyW";
            case "X": return "KeyX";
            case "Y": return "KeyY";
            case "Z": return "KeyZ";
            case "_": return "Minus";
            case ">": return "Period";
            case "": return "Quote";
            case ":": return "Semicolon";
            case "?": return "Slash";
            case "\uE00D": return "Space";
            case "\uE05C": return "Numpad0";
            case "\uE056": return "Numpad1";
            case "\uE05B": return "Numpad2";
            case "\uE055": return "Numpad3";
            case "\uE058": return "Numpad4";
            case "\uE05A": return "Numpad6";
            case "\uE057": return "Numpad7";
            case "\uE059": return "Numpad8";
            case "\uE054": return "Numpad9";
            case "\uE05D": return "NumpadDecimal";

            default: return null;
        }
    }
}
