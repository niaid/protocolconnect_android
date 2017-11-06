package niaid.lsb.protocolconnect;

import android.app.Application;

import niaid.lsb.protocolconnect.server.Controller;

/**
 * Custom application class to create the server controller in the global environment.
 */
public class CustomApplication extends Application {

    public Controller controller;

    @Override
    public void onCreate() {
        super.onCreate();
        this.controller = new Controller();
    }

}
