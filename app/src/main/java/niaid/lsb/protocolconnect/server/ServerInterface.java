package niaid.lsb.protocolconnect.server;

import java.util.HashMap;

import niaid.lsb.protocolconnect.classes.ContactResponse;
import niaid.lsb.protocolconnect.classes.EventResponse;
import niaid.lsb.protocolconnect.classes.ForgotOrChangePasswordResponse;
import niaid.lsb.protocolconnect.classes.MessageResponse;
import niaid.lsb.protocolconnect.classes.PatientResponse;
import niaid.lsb.protocolconnect.classes.PostResponse;
import niaid.lsb.protocolconnect.classes.Subject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Defines HTTP methods for server communication.
 */
public interface ServerInterface {

    /** Get a subject's information if correct email and password are entered */
    @GET("subjects/{email}:{password}")
    Call<Subject> getSubjectDetails(@Path("email") String email, @Path("password") String password);

    /** Requests an email for resetting password */
    @GET("resetPassword/{email}")
    Call<ForgotOrChangePasswordResponse> forgotPassword(@Path("email") String email);

    /** Gets all the events for a user */
    @GET("events/subject_email:{email}")
    Call<EventResponse> getEvents(@Path("email") String email);

    /** Gets the most current patient response for an event */
    @GET("patient_responses/{eventId}/latest")
    Call<PatientResponse> getLatestPatientResponse(@Path("eventId") String eventId);

    /** Posts a user's response to the server */
    @POST("patient_responses/")
    Call<PostResponse> postPatientResponse(@Body HashMap<String, String> body);

    /** Changes a user's password */
    @GET("updatePassword/{email}/{old}/{new}")
    Call<ForgotOrChangePasswordResponse> changePassword(@Path("email") String email, @Path("old") String oldPass, @Path("new") String newPass);

    /** Gets a user's messages */
    @GET("subjects/{email}/messages/get/{since}")
    Call<MessageResponse> getMessages(@Path("email") String email, @Path("since") int since);

    /** Posts a user's message */
    @POST("subjects/{email}/messages/send")
    Call<PostResponse> postMessage(@Path("email") String email, @Body HashMap<String, Object> body);

    /** Gets emergency contacts */
    @GET("contact")
    Call<ContactResponse> getContacts();

}
