package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import niaid.lsb.protocolconnect.CustomApplication;
import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Subject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Log-in page activity.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // don't inflate the menu for log in page
        return true;
    }

    @Override
    public void onResume() {
        super.onResume(); // Always call the superclass method first

        // Check if there is username/password stored in SharedPreferences so user doesn't have to login every time
        SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        if (prefs.contains("email") && prefs.contains("password")) {
            final String email = prefs.getString("email", "");
            final String password = prefs.getString("password", "");

            // Get appState to access serverInterface
            CustomApplication appState = ((CustomApplication) this.getApplication());

            // Get the correct password from the server and check against password
            Call<Subject> call = appState.controller.serverInterface.getSubjectDetails(email, password);
            call.enqueue(new Callback<Subject>() {
                Context context = getApplicationContext();
                CharSequence text;
                int duration = Toast.LENGTH_LONG;

                @Override
                public void onResponse(Call<Subject> call, Response<Subject> response) {
                    if (response.isSuccessful()) {
                        Subject subject = response.body();
                        if (subject.getStatus().equals("success")) {
                            String correctPassword = subject.getData().get(0).getPassword();
                            String study_id = subject.getData().get(0).getStudy_id();
                            if (password.equals(correctPassword)) {

                                // Save the username and password in Shared Preferences
                                SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                                prefs.edit().putString("email", email).apply();
                                prefs.edit().putString("password", password).apply();
                                prefs.edit().putString("study_id", study_id).apply();

                                Intent intent = new Intent(MainActivity.this, AllEventsActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            text = "Incorrect email or password. Try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    } else {
                        Log.d("Error", "Server login fail from onResponse, MainActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<Subject> call, Throwable t) {
                    Log.d("Error", "Server login fail from onFailure, MainActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    t.printStackTrace();
                }
            });
        }
    }

    /** Called when the user clicks the Log In button */
    public void tryLogIn(View view) {

        EditText emailEditText = (EditText) findViewById(R.id.email);
        EditText passwordEditText = (EditText) findViewById(R.id.password);
        final String email = emailEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        // Get appState to access serverInterface
        CustomApplication appState = ((CustomApplication)this.getApplication());

        // Get the correct password from the server and check against entered password
        Call<Subject> call = appState.controller.serverInterface.getSubjectDetails(email, password);
        call.enqueue(new Callback<Subject>() {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;

            @Override
            public void onResponse(Call<Subject> call, Response<Subject> response) {
                if (response.isSuccessful()) {
                    Subject subject = response.body();
                    if (subject.getStatus().equals("success")) {
                        String correctPassword = subject.getData().get(0).getPassword();
                        if (password.equals(correctPassword)) {

                            // Save the username and password in Shared Preferences
                            SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
                            prefs.edit().putString("email", email).apply();
                            prefs.edit().putString("password", password).apply();

                            // When a user logs in for the first time, set number of read messages to 0.
                            prefs.edit().putLong("numberReadMessages", 0).apply();

                            Intent intent = new Intent(MainActivity.this, AllEventsActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        text = "Incorrect email or password. Try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } else {
                    Log.d("Error","Server login fail from onResponse, MainActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<Subject> call, Throwable t) {
                Log.d("Error","Server login fail from onFailure, MainActivity.");
                text = "Server error. Please check your connection then try again.";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                t.printStackTrace();
            }
        });

    }

    /** Called when the user clicks the Forgot Password */
    public void toForgotPassword(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

}