package niaid.lsb.protocolconnect.classes;

/**
 * Describes a Contact object.
 */
public class Contact {

    private String name;
    private String email;
    private String phoneNumber;

    public Contact(String contactName, String emailAddress, String number) {
        this.name = contactName;
        this.email = emailAddress;
        this.phoneNumber = number;
    }

    public void setContactName(String contactName) {
        this.name = contactName;
    }

    public String getContactName() {
        return this.name;
    }

    public void setEmail (String emailAddress) {
        this.email = emailAddress;
    }

    public String getEmail() {
        return this.email;
    }

    public void setPhoneNumber(String number) {
        this.phoneNumber = number;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

}
