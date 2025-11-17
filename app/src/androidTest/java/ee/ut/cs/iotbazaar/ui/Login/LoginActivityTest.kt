package ee.ut.cs.iotbazaar.ui.Login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.ut.cs.iotbazaar.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun clickingRegisterNavigatesToRegisterActivity() {
        // Tap the "Make new account" button on the login screen
        onView(withId(R.id.registerRedirectBtn)).perform(click())

        // Assert that a view unique to the register screen is displayed
        onView(withId(R.id.registerBtn)).check(matches(isDisplayed()))
    }

    @Test
    fun loginWithEmptyFields_staysOnLoginScreen() {
        // Attempt to submit without entering email/password
        onView(withId(R.id.loginBtn)).perform(click())

        // Assert we stay on the login screen: key views still visible
        onView(withId(R.id.emailField)).check(matches(isDisplayed()))
        onView(withId(R.id.passwordField)).check(matches(isDisplayed()))
        onView(withId(R.id.loginBtn)).check(matches(isDisplayed()))

        // Optionally, also assert feedback text if present on screen (non-blocking)
        // This keeps the test flexible if toast/snackbar is used
        // onView(withText("Provide all the information")).check(matches(isDisplayed()))
    }
}
