package alexandertech.mymonteuniversityhub.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import alexandertech.mymonteuniversityhub.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new CheckLoginStatus().execute();
    }

    private class CheckLoginStatus extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // before making db calls

        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //returning false because perm login has NOT been set up therefor the isSessionvalid function in onPostExecute will not return true and show mainact.
            return false;

        }

        @Override
        protected void onPostExecute(final Boolean isSessionValid) {

            //here the session from the database is still valid... therefore we proceed with auto login
            if (isSessionValid) {
                Intent MainActivity = new Intent(SplashScreen.this, MainActivity.class);
                MainActivity.putExtra("First Name", "Javar");
                MainActivity.putExtra("Last Name", "Alexander");
                MainActivity.putExtra("Email", "Jaalexander@csumb.edu");
                MainActivity.putExtra("ID", "4");
                startActivity(MainActivity);

            }
            //session is NOT valid, therefore we go to the login screen.
            else {
                Intent LoginScreen = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(LoginScreen);
            }


            // close this activity
            finish();


        }



    }
}




