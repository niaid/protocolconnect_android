package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.ForgotOrChangePasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for changing password on first sign-in.
 */
public class FirstSigninActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_signin);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // don't inflate the menu for forgot password page
        return true;
    }

    /** Called when the user clicks the Set New Password button. */
    public void newPassword(View view) {
        // Access data in the edit text boxes
        EditText newPasswordText = (EditText) findViewById(R.id.firstsignin_new_password);
        final String newPassword = newPasswordText.getText().toString();

        EditText confirmNewPasswordText = (EditText) findViewById(R.id.firstsignin_confirm_new_password);
        String confirmNewPassword = confirmNewPasswordText.getText().toString();

        // Set up toasts
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        // Check that the fields are not empty
        if (newPassword.equals("") || confirmNewPassword.equals("")) {
            Toast toast = Toast.makeText(context, "A field was left empty.", duration);
            toast.show();
            return;
        }

        // Get correct password from shared preferences.
        String oldPassword = prefs.getString("password", "");

        if (newPassword.equals(confirmNewPassword)) {
            // Sends new password to server
            Call<ForgotOrChangePasswordResponse> call = appState.controller.serverInterface.changePassword(email, oldPassword, newPassword);
            call.enqueue(new Callback<ForgotOrChangePasswordResponse>() {
                Context context = getApplicationContext();
                CharSequence text;
                int duration = Toast.LENGTH_LONG;

                @Override
                public void onResponse(Call<ForgotOrChangePasswordResponse> call, Response<ForgotOrChangePasswordResponse> response) {
                    if (response.isSuccessful()) {
                        ForgotOrChangePasswordResponse passwordResponse = response.body();
                        if (passwordResponse.getStatus().equals("success")) {
                            int records = passwordResponse.getData().get(0).getRecords();
                            if (records == 0) {
                                Log.d("Error", "Email not found, ResetPasswordActivity");
                                text = "There was an error. Please try again.";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            } else if (records == 1) {
                                // Save new password in shared preferences
                                prefs.edit().putString("password", newPassword).apply();
                                Log.d("Success", "Password reset, ResetPasswordActivity.");
                                text = "Password has been changed.";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                // Change shared preferences to reflect that it is no longer the first log-in
                                prefs.edit().putString("firstSignin", "no").apply();

                                Intent intent = new Intent(FirstSigninActivity.this, AllEventsActivity.class);
                                finish();
                                startActivity(intent);
                            } else {
                                Log.d("Error", "Error. Records not 0 or 1, ResetPasswordActivity");
                                text = "Server error. Please check your connection then try again.";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        } else {
                            Log.d("Error", "Status was fail, ResetPasswordActivity.");
                            text = "Server error. Please check your connection then try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    } else {
                        Log.d("Error", "Server login fail from onResponse, ResetPasswordActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<ForgotOrChangePasswordResponse> call, Throwable t) {
                    Log.d("Error", "Server login fail from onFailure, ResetPasswordActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    t.printStackTrace();
                }
            });

        } else {
            Toast toast = Toast.makeText(context, "New passwords do not match. Try again.", duration);
            toast.show();
        }
    }
}
