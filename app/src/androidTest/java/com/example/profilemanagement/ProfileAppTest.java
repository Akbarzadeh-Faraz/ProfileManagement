package com.example.profilemanagement;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.SystemClock;
import android.widget.EditText;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import ui.DiaryEntryActivity;
import ui.LoginActivity;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // Enforce alphabetical order
public class ProfileAppTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void TC001_testLoginFailure() {
        // Enter a non-existent username and password
        onView(withId(R.id.username)).perform(typeText("jd2001"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("Passwd#1"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.login_button)).perform(click());

        // Verify that an error message is displayed
        onView(withId(R.id.error_text)).check(matches(withText("Invalid credentials")));
    }

    @Test
    public void TC002_testRegistrationSuccess() {
        // Navigate to the registration screen from the login screen
        onView(withId(R.id.register_button)).perform(click());

        // Fill in registration fields
        onView(withId(R.id.username)).perform(typeText("jd2001"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("Passwd#1"), closeSoftKeyboard());
        onView(withId(R.id.full_name)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.date_of_birth)).perform(typeText("2001-11-21"), closeSoftKeyboard());
        onView(withId(R.id.address)).perform(typeText("4700 Keele St, Toronto, ON, M3J 1P3"), closeSoftKeyboard());
        onView(withId(R.id.phone_number)).perform(typeText("555-123-4567"), closeSoftKeyboard());

        // Click the register button
        onView(withId(R.id.register_button)).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Verify redirection back to LoginActivity
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }

    @Test
    public void TC003_testLoginSuccess() {
        // Enter valid username and password
        onView(withId(R.id.username)).perform(typeText("jd2001"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("Passwd#1"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.login_button)).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Verify navigation to ProfileActivity
        onView(withId(R.id.fab_add_entry)).check(matches(isDisplayed()));
    }

    @Test
    public void TC004_testEditProfile() {
        // Assuming the user is logged in and on the profile page
        // Navigate to the edit profile screen
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Edit Profile")).perform(click());

        // Modify the full name field
        onView(withId(R.id.address)).perform(replaceText(
                "4700 Keele St, North York, ON, M3J 1P3"), closeSoftKeyboard());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Click the save button
        onView(withId(R.id.save_button)).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Verify return to ProfileActivity with updated information
        onView(withId(R.id.fab_add_entry)).check(matches(isDisplayed()));
    }

    @Test
    public void TC005_AddEntry() {
        // Click the "Add entry" button
        onView(withId(R.id.add_entry_button)).perform(click());

        // Verify navigation to DiaryEntryActivity
        intended(hasComponent(DiaryEntryActivity.class.getName()));

        // Enter diary content
        onView(withId(R.id.title)).perform(typeText("First note"), closeSoftKeyboard());
        onView(withId(R.id.content)).perform(typeText("Today I ate cereal!"), closeSoftKeyboard());

        // Click the save button
        onView(withId(R.id.save_button)).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Verify return to ProfileActivity
        onView(withId(R.id.fab_add_entry)).check(matches(isDisplayed()));
    }

    @Test
    public void TC006_ModifyEntry() {
        // Assuming the user is on the profile page with an entry from test 5
        // Click the existing diary entry
        onView(withText("First note")).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Click the edit button
        onView(withId(R.id.edit_button)).perform(click());

        // Modify the content
        onView(withId(R.id.title)).perform(replaceText("Breakfast"), closeSoftKeyboard());
        onView(withId(R.id.content)).perform(replaceText("Today I ate cereal! It was good!"), closeSoftKeyboard());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Click the save button
        onView(withId(R.id.save_button)).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Verify return to the profile page with updated entry
        onView(withText("Breakfast")).check(matches(isDisplayed()));
        onView(withText("Today I ate cereal! It was good!")).check(matches(isDisplayed()));
    }

    @Test
    public void TC007_Logout() {
        // Assuming the user is on the profile page
        // Open the menu and select "Log out"
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Log Out")).perform(click());

        // Add a small delay to allow the network response and navigation
        SystemClock.sleep(1500);

        // Verify redirection to the login screen
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }

    @Test
    public void TC008_DeleteProfile() {
        // Log in first (since logout happened in test 7)
        onView(withId(R.id.username)).perform(typeText("jd2001"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("Passwd#1"), closeSoftKeyboard());
        onView(withId(R.id.login_button)).perform(click());

        // Allow time for login and navigation
        SystemClock.sleep(1500);

        // Navigate to the edit profile screen
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText("Edit Profile")).perform(click());

        // Allow time for profile loading
        SystemClock.sleep(1500);

        // Scroll directly to the delete button instead of using generic swipes
        onView(withId(R.id.delete_button)).perform(scrollTo(), click());

        // Wait for first confirmation dialog
        SystemClock.sleep(500);

        // Click the "Delete" button in the first confirmation dialog (more specific matcher)
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        // Wait for password confirmation dialog
        SystemClock.sleep(500);

        // Enter password in the dialog
        onView(withClassName(Matchers.equalTo(EditText.class.getName())))
                .inRoot(isDialog())
                .perform(typeText("Passwd#1"), closeSoftKeyboard());

        // Click the "Delete" button in the password dialog (more specific matcher)
        onView(withText("Delete")).inRoot(isDialog()).perform(click());

        // Allow time for deletion and navigation
        SystemClock.sleep(1500);

        // Verify redirection to the login screen
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }
}