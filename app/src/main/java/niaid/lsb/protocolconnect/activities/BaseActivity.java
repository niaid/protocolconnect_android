package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import niaid.lsb.protocolconnect.CustomApplication;
import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Message;
import niaid.lsb.protocolconnect.classes.MessageResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Base activity that describes features for all activities - the toolbar and clicking on toolbar, checking for new messages.
 */
public class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    Timer timer;
    CustomApplication appState;
    SharedPreferences prefs;
    String email;
    long numberOfMessagesRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Get appState to access serverInterface and get email from SharedPreferences
        appState = ((CustomApplication)this.getApplication());
        prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        email = prefs.getString("email", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Option to not use toolbar on some activities */
    protected boolean useToolbar() {
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(layoutResID, null);
        configureToolbar(view);
        super.setContentView(view);
    }

    /** Configures the toolbar */
    private void configureToolbar(View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            if (useToolbar()) {
                setSupportActionBar(toolbar);
            } else {
                toolbar.setVisibility(View.GONE);
            }
        }
    }

    /** Handles clicking on menu items */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case R.id.schedule_icon:
                // Returns to schedule page and clears all other activities.
                intent = new Intent(getApplicationContext(), AllEventsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            case R.id.message_icon:
                // Start MessagesActivity
                intent = new Intent(getApplicationContext(), MessagesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;

            case R.id.change_password:
                // Start ResetPasswordActivity
                intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent);
                return true;

            case R.id.contact:
                // Start ContactsActivity
                intent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(intent);
                return true;

            case R.id.log_out:
                // Remove login credentials from shared preferences
                getApplicationContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE).edit().clear().apply();

                // Goes to log in page and clears all other activities
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /** Handles clicking on the messages icon when a new message icon is present. */
    public void message_onClick(View v) {
        // When user goes to messages page, clear the new message icon (red circle) by re-instantiating menu.
        invalidateOptionsMenu();

        // Start MessagesActivity
        Intent intent = new Intent(getApplicationContext(), MessagesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    /** Checks for new messages from the server every 10 seconds */
    protected void checkForMessages() {

        new Thread(new Runnable() {
            public void run() {
                timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        // Get appState to access serverInterface
                        appState = ((CustomApplication)getApplication());

                        // Get the number of read messages from shared preferences
                        numberOfMessagesRead = prefs.getLong("numberReadMessages", 0);

                        // Number of milliseconds since 00:00:00 Coordinated Universal Time (UTC)
                        int since = 1;

                        try {
                            Call<MessageResponse> call = appState.controller.serverInterface.getMessages(email, since);
                            call.enqueue(new Callback<MessageResponse>() {
                                Context context = getApplicationContext();
                                CharSequence text;
                                int duration = Toast.LENGTH_SHORT;

                                @Override
                                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                    if (response.isSuccessful()) {
                                        MessageResponse messageResponse = response.body();
                                        if (messageResponse.getStatus().equals("success")) {
                                            List<Message> allMessages = messageResponse.getData();
                                            long totalMessages = allMessages.size();

                                            // If there are unread messages, add red circle to message icon in menu
                                            if (totalMessages > numberOfMessagesRead) {

                                                System.out.println("NEW");
                                                newMessageNotification();
                                            }

                                        } else {
                                            Log.d("Error", "Status was fail when pulling new messages, BaseActivity.");
                                            text = "Server error. Please try again later.";
                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                        }
                                    } else {
                                        Log.d("Error","Server login fail from onResponse when pulling new messages, BaseActivity.");
                                        text = "Server error. Please try again later.";
                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<MessageResponse> call, Throwable t) {
                                    // If there was a problem with the connection, do nothing.
                                    // We have seen the error: D/OkHttp: <-- HTTP FAILED: javax.net.ssl.SSLException: Read error: ssl=0xeb8d7040: I/O error during system call, Connection reset by peer
                                    // Seems to be a problem with OkHttp
                                    // Timer will re-try in 10 seconds.
                                    Log.d("Error","Server login fail from onFailure when pulling new messages, BaseActivity.");
                                }
                            });
                        } catch (Throwable t) {
                            Log.d("Error","SSLException was caught and thrown, BaseActivity.");
                        }
                    }
                }, 0, 10000);
            }
        }).start();
    }

    /** Stops the timer that checks for new messages from the server every 10 seconds */
    protected void stopCheckForMessages() {
        timer.cancel();
    }

    /** Makes the new messages notification (red circle) visible */
    protected void newMessageNotification() {
        Toolbar myToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        View messageIcon = myToolbar.getMenu().findItem(R.id.message_icon).getActionView();
        ImageView newMessageIcon = (ImageView) messageIcon.findViewById(R.id.new_message_notification);
        newMessageIcon.setVisibility(View.VISIBLE);
    }
}