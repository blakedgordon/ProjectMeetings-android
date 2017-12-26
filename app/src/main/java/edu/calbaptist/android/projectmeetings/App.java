package edu.calbaptist.android.projectmeetings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 *  App
 *  Initiates the App and its context.
 *
 *  @author Blake Gordon, Caleb Solorio
 *  @version 1.0.0 10/30/17
 */

public class App extends Application {
    public static Context CONTEXT;
    public static SharedPreferences PREFERENCES;

    /**
     * Initiates the context of the app.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        CONTEXT = getApplicationContext();
        PREFERENCES = App.CONTEXT.getSharedPreferences(
                App.CONTEXT.getString(R.string.app_package), Context.MODE_PRIVATE);
    }
}
