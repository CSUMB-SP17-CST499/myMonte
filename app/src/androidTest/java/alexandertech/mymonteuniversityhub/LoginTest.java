package alexandertech.mymonteuniversityhub;

/**
 * Created by josephmolina on 4/19/17.
 */
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import alexandertech.mymonteuniversityhub.Activities.LoginActivity;
import alexandertech.mymonteuniversityhub.Activities.MainActivity;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginTest {

    @Rule
    public IntentsTestRule<LoginActivity> mActivity = new IntentsTestRule<LoginActivity>(LoginActivity.class);

    @Test
    public void loginTestAfterFillingForm(){
        String loginEmail = "josephmolina3388@gmail.com";
        String loginPassword = "myMonte94!";

        //Finding the email edit text and typing in the first name
        onView(withId(R.id.email)).perform(typeText(loginEmail), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText(loginPassword), closeSoftKeyboard());
        onView(withId(R.id.email_sign_in_button)).perform(click());
        onView(withText("OK"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        intended(hasComponent(MainActivity.class.getName()));

    }

}
