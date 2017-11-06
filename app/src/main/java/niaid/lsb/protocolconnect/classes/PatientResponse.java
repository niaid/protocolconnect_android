package niaid.lsb.protocolconnect.classes;

/**
 * Describes the JSON response when pulling a patient response from the server.
 */
public class PatientResponse {

    private String status;
    private Data data;

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public class Data {
        private String _id;
        private String event_id;
        private String studyflow_id;
        private String subject_email;
        private String response_text;
        private String reason_ID;
        private String response_ID;
        private String indicated_status;
        private String est_arrival;
        private String submit_time;
        private String response_details;

        // Potential fields from different versions of the server
        private String curReasonID;
        private String additional_response;

        public String get_id() {
            return this._id;
        }

        public String getEvent_id() {
            return event_id;
        }

        public String getStudyflow_id() {
            return studyflow_id;
        }

        public String getSubject_email() {
            return subject_email;
        }

        public String getResponse_text() {
            return response_text;
        }

        public String getResponse_ID() {
            return response_ID;
        }

        public String getReason_ID() {
            return reason_ID;
        }

        public String getIndicated_status() {
            return indicated_status;
        }

        public String getEst_arrival() {
            return est_arrival;
        }

        public String getSubmit_time() {
            return submit_time;
        }

        public String getResponse_details() {
            return this.response_details;
        }

        public String getCurReasonID() {
            return curReasonID;
        }

        public String getAdditional_response() {
            return additional_response;
        }
    }
}
