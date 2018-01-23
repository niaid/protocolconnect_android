package niaid.lsb.protocolconnect.classes;

import java.util.List;

/**
 * Describes the JSON response from server for user who is using the app.
 */
public class Subject {

    private String status;
    private List<Data> data;

    public String getStatus() {
        return status;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public class Data {
        private String email;
        private String study_id;
        private String studyflow_id;
        private String password;

        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getStudy_id() {
            return this.study_id;
        }

        public void setStudy_id(String id) {
            this.study_id = id;
        }

        public String getStudyflow_id() {
            return this.studyflow_id;
        }

        public void setStudyflow_id(String id) {
            this.studyflow_id = id;
        }

        public String getPassword() {
            return this.password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}