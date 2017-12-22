package edu.calbaptist.android.projectmeetings.utils.rest;

import edu.calbaptist.android.projectmeetings.exceptions.RestClientException;

/**
 *  Rest Client Callback Failure Interface
 *  Specifies the methods needed to handle RestClient exceptions.
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public interface RestClientCallbackFailure {
    /**
     * Handles purposeful server-side failures of the Rest Client.
     * @param e The exception which specifies the failure.
     */
    void onTaskFailed(RestClientException e);

    /**
     * Handles client-side failures of the Rest Client.
     * @param e The exception which specifies the failure.
     */
    void onExceptionRaised(Exception e);
}
