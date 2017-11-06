package niaid.lsb.protocolconnect.classes;

import java.util.List;

/**
 * Describes the object received when user clicks forgot password.
 */
public class ForgotOrChangePasswordResponse {

    private String status;
    private List<Data> data;

    public String getStatus() {
        return status;
    }

    public List<Data> getData() {
        return data;
    }

    public class Data {
        private int records;

        public int getRecords() {
            return records;
        }

    }

}
