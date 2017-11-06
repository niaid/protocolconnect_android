package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Event;
import niaid.lsb.protocolconnect.classes.PostResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity that shows options when user clicks not coming on an event.
 */
public class NotComingActivity extends BaseActivity {

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_coming);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        super.checkForMessages();

        // Retrieve event from shared preferences
        event = (Event) getIntent().getSerializableExtra("Event");

        // Check if reason was already given
        if (getIntent().hasExtra("ReasonId")) {
            String reasonId = getIntent().getExtras().getString("ReasonId");
            RadioGroup allButtons = (RadioGroup) findViewById(R.id.all_not_coming_buttons);
            if (reasonId != null) {
                switch (reasonId) {
                    case "A17": // I'm not feeling well
                        allButtons.check(R.id.not_feeling_well_not_coming);
                        break;
                    case "A18": // I have a transportation issue.
                        allButtons.check(R.id.transportation_issue_not_coming);
                        break;
                    case "A19": // I'm still at a previous appointment.
                        allButtons.check(R.id.previous_appointment_not_coming);
                        break;
                    case "A20": // I have another reason.
                        allButtons.check(R.id.another_reason_not_coming);
                        break;
                    default:
                        break;
                }
            }
        }

        // Check if additional information was already given
        if (getIntent().hasExtra("ResponseDetails")) {
            String responseDetails = getIntent().getExtras().getString("ResponseDetails");
            EditText addinfo = (EditText) findViewById(R.id.additional_info_not_coming);
            addinfo.setText(responseDetails);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopCheckForMessages();
    }

    /** Called when the user clicks the Submit button */
    public void submitResponse(View view) {
        RadioGroup allButtons = (RadioGroup) findViewById(R.id.all_not_coming_buttons);
        String response_ID = "A10";
        String response_text = "A10: No, I can't attend.";
        String reason_ID = null;
        HashMap<String, String> postBody = new HashMap<>();

        int selected = allButtons.getCheckedRadioButtonId();
        if (selected == -1) {
            Context context = getApplicationContext();
            CharSequence text = "Nothing selected.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else {
            postBody.put("event_id", event.getId());
            postBody.put("studyflow_id", event.getStudyFlowId());
            postBody.put("subject_email", event.getSubjectEmail());
            postBody.put("response_text", response_text);
            postBody.put("response_ID", response_ID);

            // Extract the selected reason
            switch(selected) {
                case R.id.not_feeling_well_not_coming:
                    reason_ID = "A17";
                    break;
                case R.id.transportation_issue_not_coming:
                    reason_ID = "A18";
                    break;
                case R.id.previous_appointment_not_coming:
                    reason_ID = "A19";
                    break;
                case R.id.another_reason_not_coming:
                    reason_ID = "A20";
                    break;
            }
            postBody.put("curReasonID", reason_ID); //called reason_ID in JSON response and GET methods

            // Get current time
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date current = Calendar.getInstance().getTime();
            String currentTime = df.format(current);
            postBody.put("submit_time", currentTime);

            // Get additional information
            EditText addInfoView = (EditText) findViewById(R.id.additional_info_not_coming);
            String addInfo = addInfoView.getText().toString();
            if (!addInfo.equals("")) {
                postBody.put("additional_response", addInfo); //called response_details in JSON response and GET methods
            }

            // Post the response to the server
            Call<PostResponse> call = appState.controller.serverInterface.postPatientResponse(postBody);
            call.enqueue(new Callback<PostResponse>() {
                Context context = getApplicationContext();
                CharSequence text;
                int duration = Toast.LENGTH_LONG;

                @Override
                public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                    if (response.isSuccessful()) {
                        PostResponse postResponse = response.body();
                        if (postResponse.getStatus().equals("success")) {
                            Intent intent = new Intent(context, AllEventsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            text = "Server error. Your response was not posted. Please check your connection then try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    } else {
                        Log.d("Error","Server post fail from onResponse, NotComingActivity.");
                        text = "Server error. Your response was not posted. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<PostResponse> call, Throwable t) {
                    Log.d("Error","Server post fail from onFailure, NotComingActivity.");
                    text = "Server error. Your response was not posted. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    t.printStackTrace();
                }
            });
        }
    }

    /** Called when the user clicks the Cancel button */
    public void cancelResponse(View view) {
        finish();
    }
}
