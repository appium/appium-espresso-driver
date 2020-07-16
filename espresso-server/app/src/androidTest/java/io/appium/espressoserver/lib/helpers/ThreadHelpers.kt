package io.appium.espressoserver.lib.helpers

fun getThreadDump() : String {
    val threadDetails = StringBuilder()
    val activeCount = Thread.activeCount()
    val threads = arrayOfNulls<Thread>(activeCount)
    Thread.enumerate(threads)
    for (thread in threads.filterNotNull()) {
        threadDetails.append("\n\n ThreadName: ${thread.name}: State: ${thread.state}")
        for (stackTraceElement in thread.stackTrace) {
            threadDetails.append("\n\t\t$stackTraceElement")
        }
    }
    return threadDetails.toString();
}
