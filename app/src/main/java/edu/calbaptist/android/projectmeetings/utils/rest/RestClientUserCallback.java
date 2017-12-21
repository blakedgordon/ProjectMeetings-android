package edu.calbaptist.android.projectmeetings.utils.rest;

import edu.calbaptist.android.projectmeetings.models.User;

/**
 *  Rest Client User Callback Interface
 *  Specifies the functionality needed to handle a User callback
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public interface RestClientUserCallback extends RestClientCallbackFailure {
    /**
     * Handles the User object passed back from RestClient
     * @param user the User object passed back.
     */
    void onTaskExecuted(User user);
}
