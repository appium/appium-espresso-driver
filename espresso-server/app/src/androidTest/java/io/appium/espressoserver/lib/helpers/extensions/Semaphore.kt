package io.appium.espressoserver.lib.helpers.extensions

import java.util.concurrent.Semaphore

fun <R> Semaphore.withPermit(block: () -> R): R {
    this.acquire()
    try {
        return block()
    } finally {
        this.release()
    }
}

fun <R> Semaphore.withPermit(block: () -> R, finalBlock: () -> Unit): R {
    this.acquire()
    try {
        return block()
    } finally {
        finalBlock()
        this.release()
    }
}
