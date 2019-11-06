package io.appium.espressoserver.test.helpers

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import io.appium.espressoserver.lib.helpers.AndroidLogger

class AppiumAndroidJUnitRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        arguments?.let {
            AndroidLogger.logger.warn("====================== ($it)")
        }
        super.onCreate(arguments)
    }
}

//
//public void onCreate(Bundle arguments) {
//
//    if (null != arguments) {
//        BAR = (String) arguments.get("foo"));
//    }
//    super.onCreate(arguments);
//}