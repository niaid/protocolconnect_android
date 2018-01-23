package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Contact;
import niaid.lsb.protocolconnect.classes.ContactListAdapter;
import niaid.lsb.protocolconnect.classes.ContactResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity that lists all contacts.
 */
public class ContactsActivity extends BaseActivity {

    private List<Contact> allContacts;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        allContacts = new ArrayList<>();

        // Get the list view
        lv = (ListView) findViewById(R.id.contact_list);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        super.checkForMessages();

        // Get Study ID from shared preferences
        SharedPreferences prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String study_id = prefs.getString("study_id", "");

        // Get contact information from API
        Call<ContactResponse> call = appState.controller.serverInterface.getContacts(study_id);
        call.enqueue(new Callback<ContactResponse>() {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;

            @Override
            public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                if (response.isSuccessful()) {
                    ContactResponse contactResponse = response.body();
                    if (contactResponse.getStatus().equals("success")) {
                        allContacts = contactResponse.getData();

                        ContactListAdapter contactAdapter = new ContactListAdapter(context, allContacts);
                        lv.setAdapter(contactAdapter);
                    } else {
                        Log.d("Error", "Status was fail, ContactsActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } else {
                    Log.d("Error","Server login fail from onResponse, ContactsActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ContactResponse> call, Throwable t) {
                // If there was a problem with the connection, do nothing.
                // We have seen the error: D/OkHttp: <-- HTTP FAILED: javax.net.ssl.SSLException: Read error: ssl=0xeb8d7040: I/O error during system call, Connection reset by peer
                // Seems to be a problem with OkHttp
                Log.d("Error","Server login fail from onFailure, ContactsActivity.");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopCheckForMessages();
    }
}
