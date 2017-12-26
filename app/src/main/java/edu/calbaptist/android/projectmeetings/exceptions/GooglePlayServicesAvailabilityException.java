package edu.calbaptist.android.projectmeetings.exceptions;

/**
 *  Google Play Services Availability Exception
 *  Handles an exception which occurs when Google Play is unavailable.
 *
 *  @author Blake Gordon
 *  @version 1.0.0 10/31/17
 */

public class GooglePlayServicesAvailabilityException extends Exception {
    public int mConnectionStatusCode;

    /**
     * Thrown when Google Play is unavailable.
     * @param connectionStatusCode The status code given by Google Play.
     */
    public GooglePlayServicesAvailabilityException(int connectionStatusCode) {
        this.mConnectionStatusCode = connectionStatusCode;
    }
}
