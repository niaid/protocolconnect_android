package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import niaid.lsb.protocolconnect.CustomApplication;
import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.ForgotOrChangePasswordResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity when a user clicks forgot password.
 */
public class ForgotPasswordActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // don't inflate the menu for forgot password page
        return true;
    }

    /** Called when the user clicks the Reset Password button. Redirects user back to log in page */
    public void resetPassword(View view) {

        EditText emailEditText = (EditText) findViewById(R.id.forgot_pw_email);
        final String email = emailEditText.getText().toString();
        if (email.equals("")) {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;
            text = "Please enter an email address.";
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        // Get appState to access serverInterface
        CustomApplication appState = ((CustomApplication)this.getApplication());

        // Check if entered email is in server and tells server to send email to user with temporary password.
        Call<ForgotOrChangePasswordResponse> call = appState.controller.serverInterface.forgotPassword(email);
        call.enqueue(new Callback<ForgotOrChangePasswordResponse>() {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;

            @Override
            public void onResponse(Call<ForgotOrChangePasswordResponse> call, Response<ForgotOrChangePasswordResponse> response) {
                if (response.isSuccessful()) {
                    ForgotOrChangePasswordResponse forgotPasswordResponse = response.body();
                    if (forgotPasswordResponse.getStatus().equals("success")) {
                        int records = forgotPasswordResponse.getData().get(0).getRecords();
                        if (records == 0) {
                            Log.d("Error", "Email not found, ForgotPasswordActivity");
                            text = "Email was not found. Please try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        } else if (records == 1) {
                            Log.d("Success", "Password reset, ForgotPasswordActivity.");
                            text = "Temporary password email sent.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                            finish();
                        } else {
                            Log.d("Error", "Error. Records not 0 or 1, ForgotPasswordActivity");
                            text = "Server error. Please check your connection then try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    } else {
                        Log.d("Error", "Status was fail, ForgotPasswordActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } else {
                    Log.d("Error","Server login fail from onResponse, ForgotPasswordActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ForgotOrChangePasswordResponse> call, Throwable t) {
                Log.d("Error","Server login fail from onFailure, ForgotPasswordActivity.");
                text = "Server error. Please check your connection then try again.";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                t.printStackTrace();
            }
        });
    }
}