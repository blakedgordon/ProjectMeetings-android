package edu.calbaptist.android.projectmeetings;

import android.app.Application;
import android.content.Context;

/**
 * Created by blakegordon on 10/30/17.
 */

public class App extends Application {
    public static Context context;

    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
