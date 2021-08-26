package com.comp4905.triviagameapp.test
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.comp4905.triviagameapp.MainActivity
import com.comp4905.triviagameapp.ModeSelect
import com.comp4905.triviagameapp.R
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CategorySteps {

    @Rule
    var activityRule: ActivityTestRule<ModeSelect> =
        ActivityTestRule<ModeSelect>(ModeSelect::class.java, true, false)

    @Given("The users opens the app")
    fun start_app() {
        val intent = Intent(ApplicationProvider.getApplicationContext<Context>(), ModeSelect::class.java)
        ActivityScenario.launch<ModeSelect>(intent)
    }


    @Then("The users enters the name {string}")
    fun enter_name(name: String) {
        onView(withId(R.id.usernameInput)).perform(typeText(name), closeSoftKeyboard())
    }

    @When("The user clicks regular")
    fun click_regular() {
        onView(withId(R.id.Regular)).perform(click())
    }

    @When("The user clicks driving")
    fun click_driving() {
        onView(withId(R.id.Driving)).perform(click())
    }

    @Then("The user is still on the mode select page")
    fun click_page() {

        onView(withId(R.id.userNameText)).check(matches(isDisplayed()))
    }

    @Then("The user is on the category select page")
    fun cat_page() {
        Intents.init()
        val intent = Intent(ApplicationProvider.getApplicationContext<Context>(), MainActivity::class.java)
        intended(hasComponent(ModeSelect::class.java.name))
        Intents.release()
    }

    @Then("Sends the correct regular mode values")
    fun regular_mode() {
        Intents.init()
        intended(
            allOf(
                hasComponent(hasShortClassName(".MainActivity")),
                toPackage("com.comp4905.triviagameapp"),
                hasExtra("mode", "regular")
            )
        )
        Intents.release()
    }

    @Then("Sends the correct driving mode values")
    fun driving_mode() {
        Intents.init()
        intended(
            allOf(
                hasComponent(hasShortClassName(".MainActivity")),
                toPackage("com.comp4905.triviagameapp"),
                hasExtra("mode", "regular")
            )
        )
        Intents.release()
    }
}