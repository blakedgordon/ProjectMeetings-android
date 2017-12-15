package edu.calbaptist.android.projectmeetings;

import org.json.JSONObject;

import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 *  Callback
 *  Specifies the various callbacks that come from the RestClient class.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/27/17
 */
public class Callback {

    public static abstract class RestClientJson {
        abstract void onTaskExecuted(JSONObject json);
        abstract void onTaskFailed(RestClientException e);
        abstract void onExceptionRaised(Exception e);
    }

    public static abstract class RestClientMeeting {
        abstract void onTaskExecuted(Meeting meeting);
        abstract void onTaskFailed(RestClientException e);
        abstract void onExceptionRaised(Exception e);

    }

    public static abstract class RestClientUser {
        abstract void onTaskExecuted(User user);
        abstract void onTaskFailed(RestClientException e);
        abstract void onExceptionRaised(Exception e);
    }

}
