package niaid.lsb.protocolconnect.classes;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Describes an Event object.
 */
public class Event implements Serializable, Comparable<Event> {

    private String id;
    private String studyFlowId;
    private String subjectEmail;
    private String name;
    private Calendar dateTime;
    private String location;
    private String notes;
    private boolean question;

    public Event(String id, String studyFlowId, String name, Calendar dateTime, String location, String notes, boolean question) {
        this.id = id;
        this.studyFlowId = studyFlowId;
        this.name = name;
        this.dateTime = dateTime;
        this.location = location;
        this.notes = notes;
        this.question = question;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setStudyFlowId(String id) {
        this.studyFlowId = id;
    }

    public String getStudyFlowId() {
        return this.studyFlowId;
    }

    public void setSubjectEmail(String email) {
        this.subjectEmail = email;
    }

    public String getSubjectEmail() {
        return this.subjectEmail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
    }

    public Calendar getDateTime() {
        return this.dateTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocation() {
        return this.location;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return this.notes;
    }

    public void setQuestion(boolean question) {
        this.question = question;
    }

    public boolean getQuestion() {
        return this.question;
    }

    @Override
    public int compareTo(Event e) {
        return getDateTime().compareTo(e.getDateTime());
    }
}
