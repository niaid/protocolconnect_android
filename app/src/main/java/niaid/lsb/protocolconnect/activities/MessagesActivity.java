package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Message;
import niaid.lsb.protocolconnect.classes.MessageListAdapter;
import niaid.lsb.protocolconnect.classes.MessageResponse;
import niaid.lsb.protocolconnect.classes.PostResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity that displays all messages.
 */
public class MessagesActivity extends BaseActivity {

    private List<Message> allMessages;
    private ListView listView;
    private MessageListAdapter messageAdapter;
    private long numberOfMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Clear the red circle notification on Messages icon
        invalidateOptionsMenu();

        // Number of milliseconds since 00:00:00 Coordinated Universal Time (UTC)
        int since = 1;

        // Pulls list of message objects associated with a user's email
        Call<MessageResponse> call = appState.controller.serverInterface.getMessages(email, since);
        call.enqueue(new Callback<MessageResponse>() {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;

            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    MessageResponse messageResponse = response.body();
                    if (messageResponse.getStatus().equals("success")) {
                        allMessages = messageResponse.getData();

                        // Get the listview
                        listView = (ListView) findViewById(R.id.message_list);
                        messageAdapter = new MessageListAdapter(context, R.id.message_text, allMessages);

                        // Setting the list adapter
                        listView.setAdapter(messageAdapter);

                        // Change number of read messages
                        numberOfMessages = allMessages.size();
                        prefs.edit().putLong("numberReadMessages", numberOfMessages).apply();

                        // Check for new messages every 10 seconds
                        checkForNewMessages();

                    } else {
                        Log.d("Error", "Status was fail, MessagesActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } else {
                    Log.d("Error","Server login fail from onResponse, MessagesActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                // If there was a problem with the connection, do nothing.
                // We have seen the error: D/OkHttp: <-- HTTP FAILED: javax.net.ssl.SSLException: Read error: ssl=0xeb8d7040: I/O error during system call, Connection reset by peer
                // Seems to be a problem with OkHttp
                Log.d("Error","Server login fail from onFailure, MessagesActivity.");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdatingMessages();
    }

    /** Called when a user clicks the send button in the text box */
    public void sendMessage(View view) {
        EditText message = (EditText) findViewById(R.id.message_send);
        String messageText = message.getText().toString();

        if (messageText.matches("")) {
            Toast.makeText(this, "You did not enter a message.", Toast.LENGTH_LONG).show();
        } else {
            // Create new message object
            Message newMessage = new Message(false, Calendar.getInstance(), messageText);
            allMessages.add(newMessage);

            // Send the message to the server
            HashMap<String, Object> postBody = new HashMap<>();
            postBody.put("is_to_patient", 0);
            postBody.put("content", messageText);

            // Get current epoch time in seconds
            long epochCurrent = System.currentTimeMillis()/ 1000L;
            postBody.put("epoch", epochCurrent);

            // Post the response to the server
            Call<PostResponse> call = appState.controller.serverInterface.postMessage(email, postBody);
            call.enqueue(new Callback<PostResponse>() {
                Context context = getApplicationContext();
                CharSequence text;
                int duration = Toast.LENGTH_LONG;

                @Override
                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        PostResponse postResponse = response.body();
                        if (postResponse.getStatus().equals("success")) {
                            // Clear the edit text view
                            EditText messageEditText = (EditText) findViewById(R.id.message_send);
                            messageEditText.setText("");

                            // Refresh activity
                            messageAdapter.notifyDataSetChanged();

                            // Change number of read messages
                            numberOfMessages = numberOfMessages + 1;
                            prefs.edit().putLong("numberReadMessages", numberOfMessages).apply();
                        } else {
                            text = "Server error. Please check your connection then try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    } else {
                        Log.d("Error","Server post fail from onResponse, MessagesActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<PostResponse> call, Throwable t) {
                    Log.d("Error","Server post fail from onFailure, MessagesActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    t.printStackTrace();
                }
            });
        }
    }

    /** Check for new messages every 10 seconds */
    private void checkForNewMessages() {

        new Thread(new Runnable() {
            public void run() {
                timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        // Get the number of read messages from shared preferences
                        numberOfMessagesRead = prefs.getLong("numberReadMessages", 0);

                        // Number of milliseconds since 00:00:00 Coordinated Universal Time (UTC)
                        int since = 1;

                        try {
                            Call<MessageResponse> call = appState.controller.serverInterface.getMessages(email, since);
                            call.enqueue(new Callback<MessageResponse>() {
                                Context context = getApplicationContext();
                                CharSequence text;
                                int duration = Toast.LENGTH_LONG;

                                @Override
                                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                                    if (response.isSuccessful()) {
                                        MessageResponse messageResponse = response.body();
                                        if (messageResponse.getStatus().equals("success")) {
                                            List<Message> retrievedMessages = messageResponse.getData();
                                            long totalMessages = retrievedMessages.size();

                                            // If there are unread messages, add red circle to message icon in menu
                                            if (totalMessages > numberOfMessagesRead) {

                                                // Update the list with the new message
                                                allMessages.addAll(retrievedMessages);
                                                messageAdapter.notifyDataSetChanged();

                                                // Change number of read messages
                                                prefs.edit().putLong("numberReadMessages", totalMessages).apply();
                                            }

                                        } else {
                                            Log.d("Error", "Status was fail when pulling new messages, AllEventsActivity.");
                                            text = "Server error. Please check your connection then try again.";
                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                        }
                                    } else {
                                        Log.d("Error", "Server login fail from onResponse when pulling new messages, AllEventsActivity.");
                                        text = "Server error. Please check your connection then try again.";
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
                                    Log.d("Error","Server login fail from onFailure when pulling new messages, MessagesActivity.");
                                }
                            });
                        } catch (Throwable t) {
                            Log.d("Error","SSLException was caught and thrown, MessagesActivity.");
                        }
                    }
                }, 0, 10000);
            }
        }).start();
    }

    /** Stops the timer that checks for new messages from the server every 10 seconds */
    private void stopUpdatingMessages() {
        timer.cancel();
    }
}
