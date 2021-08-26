package com.comp4905.triviagameapp.test

import io.cucumber.android.runner.CucumberAndroidJUnitRunner;
import io.cucumber.junit.CucumberOptions;

@CucumberOptions(
    features = ["features"],
    strict = true
)
class TriviaAppTestRunner : CucumberAndroidJUnitRunner() {
}