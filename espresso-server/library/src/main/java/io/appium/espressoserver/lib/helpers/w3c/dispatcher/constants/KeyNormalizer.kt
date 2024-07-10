package io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants

object KeyNormalizer {
    private val normalizedKeyMap = mutableMapOf<String, String>()

    fun toNormalizedKey(key: String): String {
        return if (normalizedKeyMap.containsKey(key)) {
            normalizedKeyMap[key]!!
        } else key
    }

    init {
        normalizedKeyMap["\uE000"] = NormalizedKeys.UNIDENTIFIED
        normalizedKeyMap["\uE001"] = NormalizedKeys.CANCEL
        normalizedKeyMap["\uE002"] = NormalizedKeys.HELP
        normalizedKeyMap["\uE003"] = NormalizedKeys.BACKSPACE
        normalizedKeyMap["\uE004"] = NormalizedKeys.TAB
        normalizedKeyMap["\uE005"] = NormalizedKeys.CLEAR
        normalizedKeyMap["\uE006"] = NormalizedKeys.RETURN
        normalizedKeyMap["\uE007"] = NormalizedKeys.ENTER
        normalizedKeyMap["\uE008"] = NormalizedKeys.SHIFT
        normalizedKeyMap["\uE009"] = NormalizedKeys.CONTROL
        normalizedKeyMap["\uE00A"] = NormalizedKeys.ALT
        normalizedKeyMap["\uE00B"] = NormalizedKeys.PAUSE
        normalizedKeyMap["\uE00C"] = NormalizedKeys.ESCAPE
        normalizedKeyMap["\uE00D"] = NormalizedKeys.WHITESPACE
        normalizedKeyMap["\uE00E"] = NormalizedKeys.PAGEUP
        normalizedKeyMap["\uE00F"] = NormalizedKeys.PAGEDOWN
        normalizedKeyMap["\uE010"] = NormalizedKeys.END
        normalizedKeyMap["\uE011"] = NormalizedKeys.HOME
        normalizedKeyMap["\uE012"] = NormalizedKeys.ARROW_LEFT
        normalizedKeyMap["\uE013"] = NormalizedKeys.ARROW_UP
        normalizedKeyMap["\uE014"] = NormalizedKeys.ARROW_RIGHT
        normalizedKeyMap["\uE015"] = NormalizedKeys.ARROW_DOWN
        normalizedKeyMap["\uE016"] = NormalizedKeys.INSERT
        normalizedKeyMap["\uE017"] = NormalizedKeys.DELETE
        normalizedKeyMap["\uE018"] = NormalizedKeys.SEMICOLON
        normalizedKeyMap["\uE019"] = NormalizedKeys.EQUALS
        normalizedKeyMap["\uE01A"] = NormalizedKeys.ZERO
        normalizedKeyMap["\uE01B"] = NormalizedKeys.ONE
        normalizedKeyMap["\uE01C"] = NormalizedKeys.TWO
        normalizedKeyMap["\uE01D"] = NormalizedKeys.THREE
        normalizedKeyMap["\uE01E"] = NormalizedKeys.FOUR
        normalizedKeyMap["\uE01F"] = NormalizedKeys.FIVE
        normalizedKeyMap["\uE020"] = NormalizedKeys.SIX
        normalizedKeyMap["\uE021"] = NormalizedKeys.SEVEN
        normalizedKeyMap["\uE022"] = NormalizedKeys.EIGHT
        normalizedKeyMap["\uE023"] = NormalizedKeys.NINE
        normalizedKeyMap["\uE024"] = NormalizedKeys.ASTERISK
        normalizedKeyMap["\uE025"] = NormalizedKeys.PLUS
        normalizedKeyMap["\uE026"] = NormalizedKeys.COMMA
        normalizedKeyMap["\uE027"] = NormalizedKeys.HYPHEN
        normalizedKeyMap["\uE028"] = NormalizedKeys.PERIOD
        normalizedKeyMap["\uE029"] = NormalizedKeys.FORWARD_SLASH
        normalizedKeyMap["\uE031"] = NormalizedKeys.F1
        normalizedKeyMap["\uE032"] = NormalizedKeys.F2
        normalizedKeyMap["\uE033"] = NormalizedKeys.F3
        normalizedKeyMap["\uE034"] = NormalizedKeys.F4
        normalizedKeyMap["\uE035"] = NormalizedKeys.F5
        normalizedKeyMap["\uE036"] = NormalizedKeys.F6
        normalizedKeyMap["\uE037"] = NormalizedKeys.F7
        normalizedKeyMap["\uE038"] = NormalizedKeys.F8
        normalizedKeyMap["\uE039"] = NormalizedKeys.F9
        normalizedKeyMap["\uE03A"] = NormalizedKeys.F10
        normalizedKeyMap["\uE03B"] = NormalizedKeys.F11
        normalizedKeyMap["\uE03C"] = NormalizedKeys.F12
        normalizedKeyMap["\uE03D"] = NormalizedKeys.META
        normalizedKeyMap["\uE040"] = NormalizedKeys.ZENKAKU_HANKAKU
        normalizedKeyMap["\uE050"] = NormalizedKeys.SHIFT
        normalizedKeyMap["\uE051"] = NormalizedKeys.CONTROL
        normalizedKeyMap["\uE052"] = NormalizedKeys.ALT
        normalizedKeyMap["\uE053"] = NormalizedKeys.META
        normalizedKeyMap["\uE054"] = NormalizedKeys.PAGEUP
        normalizedKeyMap["\uE055"] = NormalizedKeys.PAGEDOWN
        normalizedKeyMap["\uE056"] = NormalizedKeys.END
        normalizedKeyMap["\uE057"] = NormalizedKeys.HOME
        normalizedKeyMap["\uE058"] = NormalizedKeys.ARROW_LEFT
        normalizedKeyMap["\uE059"] = NormalizedKeys.ARROW_UP
        normalizedKeyMap["\uE05A"] = NormalizedKeys.ARROW_RIGHT
        normalizedKeyMap["\uE05B"] = NormalizedKeys.ARROW_DOWN
        normalizedKeyMap["\uE05C"] = NormalizedKeys.INSERT
        normalizedKeyMap["\uE05D"] = NormalizedKeys.DELETE
    }
}