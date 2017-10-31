package edu.calbaptist.android.projectmeetings.Exceptions;

/**
 * Created by blakegordon on 10/31/17.
 */

public class RequestPermissionException extends Exception {
    public RequestPermissionException() {
    }

    public RequestPermissionException(String message) {
        super(message);
    }
}
