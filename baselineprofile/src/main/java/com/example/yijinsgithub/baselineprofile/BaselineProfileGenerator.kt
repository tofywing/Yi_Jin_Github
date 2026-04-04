package com.example.yijinsgithub.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.example.yijinsgithub",
        includeInStartupProfile = true
    ) {
        // Start the app
        pressHome()
        startActivityAndWait()

        // Wait for the app to be visible
        device.wait(Until.hasObject(By.pkg("com.example.yijinsgithub")), 10000)

        // Wait for the scrollable list to appear. 
        // We increase the timeout to handle network latency for popular repos.
        val scrollableList = device.wait(Until.hasObject(By.scrollable(true)), 15000)

        if (scrollableList != null) {
            val column = device.findObject(By.scrollable(true))
            if (column != null) {
                column.setGestureMargin(device.displayWidth / 10)
                // Perform a few flings to capture scroll performance profiles
                column.fling(Direction.DOWN)
                device.waitForIdle()
                column.fling(Direction.UP)
                device.waitForIdle()
            }
        } else {
            // If the list didn't appear (e.g., no internet or stuck on login), 
            // at least we captured the startup profile.
            // We can also try to find the login button to interact with something.
            val loginButton = device.findObject(By.textContains("Login"))
            loginButton?.click()
            device.waitForIdle()
        }
    }
}
