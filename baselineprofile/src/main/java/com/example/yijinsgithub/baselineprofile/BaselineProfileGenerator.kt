package com.example.yijinsgithub.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        // This block defines the user journeys to profile.
        // For now, we just start the app.
        pressHome()
        startActivityAndWait()
    }
}
