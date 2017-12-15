package edu.calbaptist.android.projectmeetings.exceptions;

/**
 * Created by blakegordon on 10/31/17.
 */

public class GooglePlayServicesAvailabilityException extends Exception {
    public int connectionStatusCode;

    public GooglePlayServicesAvailabilityException(int connectionStatusCode) {
        this.connectionStatusCode = connectionStatusCode;
    }

    public GooglePlayServicesAvailabilityException(int connectionStatusCode, String message) {
        super(message);
        this.connectionStatusCode = connectionStatusCode;
    }
}
