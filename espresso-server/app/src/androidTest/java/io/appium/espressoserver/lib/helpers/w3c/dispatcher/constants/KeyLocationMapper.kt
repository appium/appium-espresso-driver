package io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants

/**
 * Implement table found in 17.4.2 that maps characters to 'location'
 *
 * Not using constants or enums because I don't think we'll actually need these in any
 * specific implementation
 * @param key Raw key
 * @return The location of the key or default 0
 */
fun getLocation(key: String?): Int {
    return when (key) {
        "\uE007" -> 1
        "\uE008" -> 1
        "\uE009" -> 1
        "\uE00A" -> 1
        "\uE01A" -> 3
        "\uE01B" -> 3
        "\uE01C" -> 3
        "\uE01D" -> 3
        "\uE01E" -> 3
        "\uE01F" -> 3
        "\uE020" -> 3
        "\uE021" -> 3
        "\uE022" -> 3
        "\uE023" -> 3
        "\uE024" -> 3
        "\uE025" -> 3
        "\uE026" -> 3
        "\uE027" -> 3
        "\uE028" -> 3
        "\uE029" -> 3
        "\uE03D" -> 1
        "\uE050" -> 2
        "\uE051" -> 2
        "\uE052" -> 2
        "\uE053" -> 2
        "\uE054" -> 3
        "\uE055" -> 3
        "\uE056" -> 3
        "\uE057" -> 3
        "\uE058" -> 3
        "\uE059" -> 3
        "\uE05A" -> 3
        "\uE05B" -> 3
        "\uE05C" -> 3
        "\uE05D" -> 3
        else -> 0
    }
}