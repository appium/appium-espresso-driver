package io.appium.espressoserver.test.assets;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Helpers {

    public static String readAssetFile(String filename) throws IOException {
        String projectDir = System.getProperty("user.dir");
        String assetPath = String.format("%s/app/src/test/java/io/appium/espressoserver/test/assets/", projectDir);
        File file = new File(String.format("%s/%s", assetPath, filename));
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner = new Scanner(file);
        String lineSeparator = System.getProperty("line.separator");

        try {
            while(scanner.hasNextLine()) {
                fileContents.append(scanner.nextLine() + lineSeparator);
            }
            return fileContents.toString();
        } finally {
            scanner.close();
        }
    }
}
