package edu.calbaptist.android.projectmeetings.utils.rest;

import edu.calbaptist.android.projectmeetings.models.Meeting;

/**
 *  Rest Client Meeting Callback Interface
 *  Specifies the functionality needed to handle a Meeting callback
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public interface RestClientMeetingCallback extends RestClientCallbackFailure {
    /**
     * Handles the Meeting passed back from RestClient
     * @param meeting the Meeting passed back.
     */
    void onTaskExecuted(Meeting meeting);
}
