package edu.calbaptist.android.projectmeetings;

import android.app.Application;
import android.content.Context;

/**
 *  App
 *  Initiates the App and its context.
 *
 *  @author Blake Gordon
 *  @version 1.0.0 10/30/17
 */

public class App extends Application {
    public static Context context;

    /**
     * Initiates the context of the app.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
