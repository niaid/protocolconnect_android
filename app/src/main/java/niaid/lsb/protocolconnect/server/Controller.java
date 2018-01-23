package niaid.lsb.protocolconnect.server;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Sets up the server connection.
 */
public final class Controller {

    // Replace BASE_URL with your own server info (see README for more info)
    public static final String BASE_URL = "XXX";

    public ServerInterface serverInterface;

    public Controller() {

        // Define the interceptor, add authentication headers
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                // Replace credentials with your own server info (see README for more info)
                String authToken = Credentials.basic("XXX", "XXX");

                Request newRequest = chain.request().newBuilder().addHeader("Authorization", authToken).build();
                return chain.proceed(newRequest);
            }
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        builder.addInterceptor(logging);
        OkHttpClient httpClient = builder.build();

        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).client(httpClient).build();

        this.serverInterface = retrofit.create(ServerInterface.class);

    }

}
