package io.appium.espressoserver.test.assets

import java.io.File
import java.io.IOException
import java.util.*

@Throws(IOException::class)
fun readAssetFile(filename: String?): String {
    val projectDir = System.getProperty("user.dir")
    val assetPath = "$projectDir/src/test/java/io/appium/espressoserver/test/assets/"
    val file = File("$assetPath/$filename")
    val fileContents = StringBuilder(file.length().toInt())
    Scanner(file).use { scanner ->
        val lineSeparator = System.getProperty("line.separator")
        while (scanner.hasNextLine()) {
            fileContents.append(scanner.nextLine()).append(lineSeparator)
        }
        return fileContents.toString()
    }
}