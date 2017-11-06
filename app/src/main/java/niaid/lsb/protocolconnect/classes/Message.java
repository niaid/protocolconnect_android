package niaid.lsb.protocolconnect.classes;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Describes a Message object.
 */
public class Message implements Serializable, Comparable<Message> {

    private boolean isToSubject; // If true, message is from staff to subject; if false, from subject to staff
    private Calendar date;
    private String message;

    public Message(boolean isToSubject, Calendar date, String message) {
        this.isToSubject = isToSubject;
        this.date = date;
        this.message = message;
    }

    public void setIsToSubject(boolean isToSubject) {
        this.isToSubject = isToSubject;
    }

    public boolean getIsToSubject() {
        return this.isToSubject;
    }

    public void setDate (Calendar date) {
        this.date = date;
    }

    public Calendar getDate() {
        return this.date;
    }

    public String printDate() {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy h:mm:ss a", Locale.US);
        return df.format(this.date.getTime());
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public int compareTo(Message m) {
        return getDate().compareTo(m.getDate());
    }
}
