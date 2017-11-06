package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.ForgotOrChangePasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for reset password page.
 */
public class ResetPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
    }

    @Override
    public void onResume() {
        super.onResume();
        super.checkForMessages();
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopCheckForMessages();
    }

    /** Called when the user clicks the Set New Password button. */
    public void newPassword(View view) {
        // Access data in the edit text boxes
        EditText oldPasswordText = (EditText) findViewById(R.id.old_password);
        String oldPassword = oldPasswordText.getText().toString();

        EditText newPasswordText = (EditText) findViewById(R.id.new_password);
        final String newPassword = newPasswordText.getText().toString();

        EditText confirmNewPasswordText = (EditText) findViewById(R.id.confirm_new_password);
        String confirmNewPassword = confirmNewPasswordText.getText().toString();

        // Set up toasts
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        // Check that the fields are not empty
        if (oldPassword.equals("") || newPassword.equals("") || confirmNewPassword.equals("")) {
            Toast toast = Toast.makeText(context, "A field was left empty.", duration);
            toast.show();
            return;
        }

        // Get correct password from shared preferences.
        String correctPassword = prefs.getString("password", "");

        if (correctPassword.equals(oldPassword)) {
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
                                    prefs.edit().putString("password", newPassword).apply();
                                    Log.d("Success", "Password reset, ResetPasswordActivity.");
                                    text = "Password has been changed.";
                                    Toast toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                    finish();
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
                            Log.d("Error","Server login fail from onResponse, ResetPasswordActivity.");
                            text = "Server error. Please check your connection then try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ForgotOrChangePasswordResponse> call, Throwable t) {
                        Log.d("Error","Server login fail from onFailure, ResetPasswordActivity.");
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
        } else {
            Toast toast = Toast.makeText(context, "Old password is incorrect. Try again.", duration);
            toast.show();
        }
    }

    /** Called when the user clicks the Cancel button. Redirects user back to even page. */
    public void cancelNewPassword(View view) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, "Password was not changed.", duration);
        toast.show();
        finish();
    }
}