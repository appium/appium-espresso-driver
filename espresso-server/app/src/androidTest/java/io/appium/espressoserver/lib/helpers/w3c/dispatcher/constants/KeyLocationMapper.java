package io.appium.espressoserver.lib.helpers.w3c.dispatcher.constants;

public class KeyLocationMapper {
    /**
     * Implement table found in 17.4.2 that maps characters to 'location'
     *
     * Not using constants or enums because I don't think we'll actually need these in any
     * specific implementation
     * @param key Raw key
     * @return The location of the key or default 0
     */
    public static int getLocation(String key) {
        switch (key) {
            case "\uE007": return 1;
            case "\uE008": return 1;
            case "\uE009": return 1;
            case "\uE00A": return 1;
            case "\uE01A": return 3;
            case "\uE01B": return 3;
            case "\uE01C": return 3;
            case "\uE01D": return 3;
            case "\uE01E": return 3;
            case "\uE01F": return 3;
            case "\uE020": return 3;
            case "\uE021": return 3;
            case "\uE022": return 3;
            case "\uE023": return 3;
            case "\uE024": return 3;
            case "\uE025": return 3;
            case "\uE026": return 3;
            case "\uE027": return 3;
            case "\uE028": return 3;
            case "\uE029": return 3;
            case "\uE03D": return 1;
            case "\uE050": return 2;
            case "\uE051": return 2;
            case "\uE052": return 2;
            case "\uE053": return 2;
            case "\uE054": return 3;
            case "\uE055": return 3;
            case "\uE056": return 3;
            case "\uE057": return 3;
            case "\uE058": return 3;
            case "\uE059": return 3;
            case "\uE05A": return 3;
            case "\uE05B": return 3;
            case "\uE05C": return 3;
            case "\uE05D": return 3;
            default: return 0;
        }
    }
}
