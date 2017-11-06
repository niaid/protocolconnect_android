package niaid.lsb.protocolconnect.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Event;
import niaid.lsb.protocolconnect.classes.PatientResponse;
import niaid.lsb.protocolconnect.classes.PostResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for a single event.
 */
public class EventActivity extends BaseActivity {

    private Event event;
    private String reasonId;
    private String estArrival;
    private String responseDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        super.checkForMessages();

        // Retrieve event from shared preferences
        event = (Event) getIntent().getSerializableExtra("Event");

        // Fill in views for event details
        TextView eventName = (TextView) findViewById(R.id.event_name);
        TextView eventLocation = (TextView) findViewById(R.id.event_location);
        TextView eventTime = (TextView) findViewById(R.id.event_datetime);
        TextView eventNotes = (TextView) findViewById(R.id.event_notes);

        SimpleDateFormat datef = new SimpleDateFormat("EEEE, MMMM dd, yyyy, h:mm a", Locale.US);
        eventName.setText(event.getName());
        eventLocation.setText(event.getLocation());
        eventTime.setText(datef.format(event.getDateTime().getTime()));
        eventNotes.setText(event.getNotes());

        View questionViewGroup = findViewById(R.id.questionViews);
        Button noQuestionBack = (Button) findViewById(R.id.no_question_back_button);

        //If the event does not require RSVP, do not show RSVP options
        if (!event.getQuestion()) {
            questionViewGroup.setVisibility(View.GONE);
            noQuestionBack.setVisibility(View.VISIBLE);
        }

        // Pulls the current responses to an event
        Call<PatientResponse> call = appState.controller.serverInterface.getLatestPatientResponse(event.getId());
        call.enqueue(new Callback<PatientResponse>() {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;

            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                if (response.isSuccessful()) {
                    PatientResponse patientResponse = response.body();
                    if (patientResponse.getStatus().equals("success")) {

                        // Check if there was already a response
                        String responseId = patientResponse.getData().getResponse_ID();
                        if (responseId != null) {
                            RadioGroup allButtons = (RadioGroup) findViewById(R.id.all_event_buttons);
                            switch (responseId) {
                                case "A8": // Yes, I'll be there.
                                    allButtons.check(R.id.yes_ill_be_there);
                                    break;
                                case "A9": // Yes, but I'll be late.
                                    allButtons.check(R.id.yes_late);
                                    break;
                                case "A10": // No, I can't attend
                                    allButtons.check(R.id.no_cant_attend);
                                    break;
                                default:
                                    break;
                            }
                        }

                        // Store reason, est arrival time, and response details to be accessed later
                        reasonId = patientResponse.getData().getReason_ID();
                        estArrival = patientResponse.getData().getEst_arrival();
                        responseDetails = patientResponse.getData().getResponse_details();

                    } else {
                        Log.d("Error", "Status was fail, EventActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } else {
                    Log.d("Error","Server login fail from onResponse, EventActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                Log.d("Error","Server login fail from onFailure, EventActivity.");
                text = "Server error. Please check your connection then try again.";
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        super.stopCheckForMessages();
    }

    /** Called when the user clicks the Submit button */
    public void submitResponse(View view) {
        RadioGroup allButtons = (RadioGroup) findViewById(R.id.all_event_buttons);
        switch(allButtons.getCheckedRadioButtonId()) {
            case -1:
                Context context = getApplicationContext();
                CharSequence text = "Nothing selected.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                break;
            case R.id.yes_ill_be_there:
                // Format the JSON body of POST
                HashMap<String, String> postBody = new HashMap<>();
                postBody.put("event_id", event.getId());
                postBody.put("studyflow_id", event.getStudyFlowId());
                postBody.put("subject_email", event.getSubjectEmail());
                postBody.put("response_text", "A8: Yes, I'll be there.");
                postBody.put("response_ID", "A8");

                // Get current time
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date current = Calendar.getInstance().getTime();
                String currentTime = df.format(current);
                postBody.put("submit_time", currentTime);

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
                                finish();
                            } else {
                                text = "Server error. Your response was not posted. Please check your connection then try again.";
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        } else {
                            Log.d("Error","Server post fail from onResponse, EventActivity.");
                            text = "Server error. Your response was not posted. Please check your connection then try again.";
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PostResponse> call, Throwable t) {
                        Log.d("Error","Server post fail from onFailure, EventActivity.");
                        text = "Server error. Your response was not posted. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        t.printStackTrace();
                    }
                });
                break;
            case R.id.yes_late:
                // Store relevant information in intent then go to LateActivity
                Intent lateIntent = new Intent(this, LateActivity.class);
                lateIntent.putExtra("Event", event);
                if (reasonId != null) {
                    lateIntent.putExtra("ReasonId", reasonId);
                }
                if (estArrival != null) {
                    lateIntent.putExtra("EstArrival", estArrival);
                }
                if (responseDetails != null) {
                    lateIntent.putExtra("ResponseDetails", responseDetails);
                }
                startActivity(lateIntent);
                break;
            case R.id.no_cant_attend:
                // Store relevant information in intent then go to NotComingActivity
                Intent cantAttendIntent = new Intent(this, NotComingActivity.class);
                cantAttendIntent.putExtra("Event", event);
                if (reasonId != null) {
                    cantAttendIntent.putExtra("ReasonId", reasonId);
                }
                if (responseDetails != null) {
                    cantAttendIntent.putExtra("ResponseDetails", responseDetails);
                }
                startActivity(cantAttendIntent);
                break;
        }
    }

    /** Called when the user clicks the Cancel button (if it is a RSVP event) */
    public void cancelResponse(View view) {
        finish();
    }

    /** Called when the user clicks the Back button (if not a RSVP event) */
    public void noQuestionBack(View view) {
        finish();
    }
}