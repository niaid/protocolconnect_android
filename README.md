# Protocol Connect (Android app)
Protocol Connect is a smartphone app and web-based portal designed to enable more efficient communication between clinical staff and study subjects about scheduling. Research subjects can communicate any schedule changes in real-time and via one mechanism to research staff, saving both research staff and subjects’ time and increasing the likelihood of study protocol compliance.

## IMPORTANT
The code in this repository will not compile a fully-functioning app as-is. We have removed the lines of code that are specific to our own server. To modify this code for your own needs, you must set up your own server first (please see the section [Steps to modify](https://github.com/niaid/protocolconnect_android#steps-to-modify)).

For testing purposes only, please download protocolconnect_android_1.0.0.apk.

## App prototype
The APK file is meant to be used for testing purposes only. This version of the app is configured to our own server, so any communication will be through our server.

Download "protocolconnect_android_1.0.0.apk" onto your Android device and the Android system will automatically start installing it. Please configure your Settings to allow the installation of apps from unknown sources.

## Features
* After logging in, the app displays your schedule on the home page. The current day (or next closest day) will be expanded.
* Click on each event to display the time, location, and details for that event.
* Some events on the schedule server as reminders only and do not require RSVP (for example, "REMINDER: do not eat or drink for 12 hours before your appointment").
* For events that take a response, you can indicate if you will attend, be late, or can't attend.
* If you indicate you will be late, specify a time and reason. If you can't attend, specify a reason.
* Your phone will send you a reminder 1 hour before each event (this is done through Alarm Manager).
* You can send or receive messages by clicking on the chat bubble in the toolbar.
* You can also see contact information for research staff or change your password. 

## Steps to modify
1. Set up your own server (see other Github repository).
2. Open the file [Controller.java](https://github.com/niaid/protocolconnect_android/blob/master/app/src/main/java/niaid/lsb/protocolconnect/server/Controller.java).
3. Change the BASE_URL to your own server url (line 22).
4. Change the authentication token for your server (line 32).
5. If your server uses a different API, you will need to change all the Response.java files in the [classes folder](https://github.com/niaid/protocolconnect_android/tree/master/app/src/main/java/niaid/lsb/protocolconnect/classes).

## General code architecture
* The [activities](https://github.com/niaid/protocolconnect_android/tree/master/app/src/main/java/niaid/lsb/protocolconnect/activities) folder contains the code that is run on each activity. There is one activity for every screen in our app.
* The [classes](https://github.com/niaid/protocolconnect_android/tree/master/app/src/main/java/niaid/lsb/protocolconnect/classes) folder contains the code that handles server responses and defines the objects used throughout the app. Server response objects are files that end with Response.java.
* The [server](https://github.com/niaid/protocolconnect_android/tree/master/app/src/main/java/niaid/lsb/protocolconnect/server) folder contains code that handles API calls. We used the HTTP clients [OkHttp](http://square.github.io/okhttp/) and [Retrofit](http://square.github.io/retrofit/).

## Contact info
Main author: [@cliu72](https://github.com/cliu72)

Feel free to contact [@cliu72](https://github.com/cliu72) (candace.liu94@gmail.com) if you have any questions!

This app was developed in the Systems Genomics and Bioinformatics Group of the Laboratory of Systems Biology under Dr. John Tsang.
