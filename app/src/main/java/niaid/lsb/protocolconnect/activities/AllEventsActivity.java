package niaid.lsb.protocolconnect.activities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import niaid.lsb.protocolconnect.CustomApplication;
import niaid.lsb.protocolconnect.R;
import niaid.lsb.protocolconnect.classes.Event;
import niaid.lsb.protocolconnect.classes.EventResponse;
import niaid.lsb.protocolconnect.classes.ExpandableListAdapter;
import niaid.lsb.protocolconnect.classes.NotificationPublisher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for the schedule page listing all events.
 */
public class AllEventsActivity extends BaseActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Event>> listDataChildEvents;
    CustomApplication appState;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_events);

        // Get appState to access serverInterface and get email from SharedPreferences
        appState = ((CustomApplication)this.getApplication());

        // If it doesn't already exist, create ArrayList to track which events we have sent reminder notifications for
        prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        if (!prefs.contains("reminders")) {
            ArrayList arrayList = new ArrayList();
            Gson gson = new Gson();
            String json = gson.toJson(arrayList);
            prefs.edit().putString("reminders", json).apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        super.checkForMessages();

        // Get the list of events from the server
        // Putting this code in onResume allows the app to re-pull the data each time the user comes back to this page
        Call<EventResponse> call = appState.controller.serverInterface.getEvents(email);
        call.enqueue(new Callback<EventResponse>() {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_LONG;

            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful()) {
                    EventResponse eventResponse = response.body();
                    if (eventResponse.getStatus().equals("success")) {
                        List<Event> allEvents = eventResponse.getData();
                        prepareData(allEvents);

                        // Get the listview
                        expListView = (ExpandableListView) findViewById(R.id.lvExp);
                        listAdapter = new ExpandableListAdapter(context, listDataHeader, listDataChildEvents);

                        // Setting list adapter
                        expListView.setAdapter(listAdapter);

                        // Get today's date
                        Calendar current = Calendar.getInstance();
                        SimpleDateFormat datef = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);
                        String currentDate = datef.format(current.getTime());

                        // If the current day is in the list, expand the current day's schedule. If not, expand the next upcoming day.
                        if (listDataHeader.contains(currentDate)) {
                            expListView.expandGroup(listDataHeader.indexOf(currentDate));
                        } else {
                            // To find the index of the date to expand, add the current date to the list of all dates. When sorted, the index of the current date in this temporary list is the index to expand.
                            List<Calendar> tempDates = new ArrayList<>();
                            for (int i=0; i<listDataHeader.size(); i++) {
                                Calendar cal = Calendar.getInstance();
                                try {
                                    Date date = datef.parse(listDataHeader.get(i));
                                    cal.setTime(date);
                                    tempDates.add(cal);
                                } catch (java.text.ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            tempDates.add(current);
                            Collections.sort(tempDates);
                            int index = tempDates.indexOf(current);

                            // If the index is equal to the size of the list, the current date is past the last date in the schedule, so expand the very last group.
                            if (index == listDataHeader.size()) {
                                expListView.expandGroup(listDataHeader.size()-1);
                            } else {
                                expListView.expandGroup(index);
                            }
                        }

                        // Create event reminders for each event
                        for (int i=0; i<allEvents.size(); i++) {
                            Event temp = allEvents.get(i);
                            // Check if the event is in the future
                            if (temp.getDateTime().compareTo(current) >= 0) {
                                // Check if the event reminder has already been sent
                                Gson gson = new Gson();
                                String json = prefs.getString("reminders", null);
                                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                                ArrayList<String> arrayList = gson.fromJson(json, type);
                                if (!arrayList.contains(temp.getId())) {
                                    scheduleEventReminder(temp);
                                }
                            }
                        }

                        // Listview on child click listener, goes to Event page
                        expListView.setOnChildClickListener(new OnChildClickListener() {
                            @Override
                            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                                Intent intent = new Intent(v.getContext(), EventActivity.class);
                                intent.putExtra("Event", listDataChildEvents.get(listDataHeader.get(groupPosition)).get(childPosition));
                                startActivity(intent);
                                return false;
                            }
                        });
                    } else {
                        Log.d("Error", "Status was fail, AllEventsActivity.");
                        text = "Server error. Please check your connection then try again.";
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                } else {
                    Log.d("Error","Server login fail from onResponse, AllEventsActivity.");
                    text = "Server error. Please check your connection then try again.";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Log.d("Error","Server login fail from onFailure, AllEventsActivity.");
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

    /** Parses the Event objects and creates an ArrayList of dates to be displayed */
    private void prepareData(List<Event> allEvents) {
        // Sort the events by date
        Collections.sort(allEvents);

        SimpleDateFormat datef = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);

        listDataHeader = new ArrayList<>();
        listDataChildEvents = new HashMap<>();

        for (int i=0; i<allEvents.size(); i++) {
            Event oneEvent = allEvents.get(i);
            String eventDate = datef.format(oneEvent.getDateTime().getTime());
            if (listDataHeader.contains(eventDate)) {
                listDataChildEvents.get(eventDate).add(oneEvent);
            } else {
                listDataHeader.add(eventDate);
                List<Event> newList = new ArrayList<Event>();
                newList.add(oneEvent);
                listDataChildEvents.put(eventDate, newList);
            }
        }
    }

    /** Creates an event reminder for each event */
    private void scheduleEventReminder(Event event) {
        // Each event notification needs a unique id, we will use the date and time of the event as the id
        SimpleDateFormat datef = new SimpleDateFormat("MMddyyHHmm", Locale.US);
        int id = Integer.parseInt(datef.format(event.getDateTime().getTime()));

        // Create the notification
        Notification notification = getNotification(event);

        // Create the intent
        Intent intent = new Intent(this, NotificationPublisher.class);
        intent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        intent.putExtra(NotificationPublisher.NOTIFICATION, notification);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = event.getDateTime();
        // Set reminder for one hour before the event
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis()-3600000, pendingIntent);
    }

    /** Builds the notification for each event */
    private Notification getNotification(Event event) {
        // Convert date to string to display in notification
        SimpleDateFormat timef = new SimpleDateFormat("h:mm a", Locale.US);
        String time = timef.format(event.getDateTime().getTime());

        // Set up intent that directs user to schedule list if notification is clicked
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Build the notification
        Notification.Builder builder = new Notification.Builder(this);
        builder.setAutoCancel(true);
        builder.setContentTitle("Reminder: "+event.getName());
        builder.setContentText(event.getName()+" is at "+time+" at "+event.getLocation()+".");
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentIntent(intent);

        // Indicate that this event has already had a notification set for it in SharedPreferences
        Gson gson = new Gson();
        String json = prefs.getString("reminders", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> arrayList = gson.fromJson(json, type);
        arrayList.add(event.getId());
        String newJson = gson.toJson(arrayList);
        prefs.edit().putString("reminders", newJson).apply();

        // builder.build is available for API level 16 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        } else {
            return builder.getNotification();
        }
    }
}