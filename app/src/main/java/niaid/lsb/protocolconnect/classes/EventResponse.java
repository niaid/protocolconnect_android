package niaid.lsb.protocolconnect.classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Describes JSON object returned from server when asking for all the events for one person.
 */
public class EventResponse {

    private String status;
    private List<Data> data;

    public String getStatus() {
        return status;
    }

    /** Extracts the data and converts it into Event objects */
    public List<Event> getData() {
        return convertEventResponseToEventObjects();
    }

    public class Data {
        public String _id;
        public String studyflow_id;
        public String subject_id;
        public String event_template_id;
        public String subject_email;
        public String name;
        public String time;
        public String rel_date;
        public String location;
        public String notes;
        public String question;
        public String flag;

        public String getId() {
            return _id;
        }

        public String getStudyflow_id() {
            return studyflow_id;
        }

        public String getSubjectEmail() {
            return subject_email;
        }

        public String getName() {
            return name;
        }

        public String getTime() {
            return time;
        }

        public String getLocation() {
            return location;
        }

        public String getNotes() {
            return notes;
        }

        public String getQuestion() {
            return question;
        }
    }

    /** Convert the server response to Event objects. */
    public List<Event> convertEventResponseToEventObjects() {
        List<Event> allEvents = new ArrayList<>();
        for (int i=0; i<this.data.size(); i++) {
            Data oneEvent = this.data.get(i);
            boolean isQuestion;
            Calendar cal = Calendar.getInstance();

            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = df.parse(oneEvent.getTime());
                cal.setTime(date);
                // System.out.println(cal.getTime());
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            // question field is "yes" or "no" from server, convert to boolean
            try {
                if (oneEvent.getQuestion().equals("yes")) {
                    isQuestion = true;
                } else {
                    isQuestion = false;
                }
            } catch (java.lang.NullPointerException e) {
                isQuestion = false;
            }

            Event eventObject = new Event(oneEvent.getId(), oneEvent.getStudyflow_id(), oneEvent.getName(), cal, oneEvent.getLocation(), oneEvent.getNotes(), isQuestion);
            eventObject.setSubjectEmail(oneEvent.getSubjectEmail());
            allEvents.add(eventObject);
        }
        return allEvents;
    }

}
