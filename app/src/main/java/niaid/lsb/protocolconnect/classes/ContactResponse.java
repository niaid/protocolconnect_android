package niaid.lsb.protocolconnect.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes JSON object returned from server when pulling Emergency Contact information.
 */
public class ContactResponse {

    private String status;
    private List<Data> data;

    public String getStatus() {
        return status;
    }

    /** Extracts the data and converts it into Contact objects */
    public List<Contact> getData() {
        return convertContactResponseToContactObjects();
    }

    public class Data {
        private String _id;
        private String firstname;
        private String lastname;
        private String phone;
        private String email;
        private String email2;
        private String study_id;

        public String get_id() {
            return this._id;
        }

        public String getFirstname() {
            return this.firstname;
        }

        public String getLastname() {
            return this.lastname;
        }

        public String getPhone() {
            return this.phone;
        }

        public String getEmail() {
            return this.email;
        }

        public String getEmail2() {
            return this.email2;
        }

        public String getStudyId() {
            return this.study_id;
        }
    }

    /** Convert the server response to Contact objects. */
    public List<Contact> convertContactResponseToContactObjects() {
        List<Contact> allContacts = new ArrayList<>();
        for (int i=0; i<this.data.size(); i++) {
            Data oneContact = this.data.get(i);
            Contact contactObject = new Contact(oneContact.getFirstname()+ " " + oneContact.getLastname(), oneContact.getEmail(), oneContact.getPhone());
            allContacts.add(contactObject);
        }
        return allContacts;
    }

}
