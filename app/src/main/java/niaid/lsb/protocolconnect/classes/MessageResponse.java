package niaid.lsb.protocolconnect.classes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Describes the JSON response when pulling a patient's messages from the server.
 */
public class MessageResponse {

    private String status;
    private List<Data> data;

    public String getStatus() {
        return status;
    }

    /** Extracts the data and converts it into Message objects */
    public List<Message> getData() {
        return convertMessageResponseToMessageObjects();
    }

    public class Data {
        private String _id;
        private String email;
        private String content;
        private double epoch;
        private String date;
        private int is_to_patient;
        private String firstname;
        private String lastname;

        public String get_id() {
            return this._id;
        }

        public String getEmail() {
            return this.email;
        }

        public String getContent() {
            return this.content;
        }

        public double getEpoch() {
            return this.epoch;
        }

        public String getDate() {
            return this.date;
        }

        public int getIs_to_patient() {
            return this.is_to_patient;
        }

        public String getFirstname() {
            return this.firstname;
        }

        public String getLastname() {
            return this.lastname;
        }

    }

    /** Convert the server response to Message objects. */
    public List<Message> convertMessageResponseToMessageObjects() {
        List<Message> allMessages = new ArrayList<>();
        for (int i=0; i<this.data.size(); i++) {
            Data oneMessage = this.data.get(i);
            boolean isToSubject;
            Calendar cal = Calendar.getInstance();

            // Convert epoch time to milliseconds (Date constructor takes milliseconds) and cast to long
            long epoch = (long) oneMessage.getEpoch()*1000L;
            Date date = new Date(epoch);
            cal.setTime(date);

            // is_to_patient field is 0 or 1 from server, convert to boolean
            if (oneMessage.getIs_to_patient() == 1) {
                isToSubject = true;
            } else {
                isToSubject = false;
            }

            Message messageObject = new Message(isToSubject, cal, oneMessage.getContent());
            allMessages.add(messageObject);
        }

        // Sort the events by date
        Collections.sort(allMessages);

        return allMessages;
    }
}
