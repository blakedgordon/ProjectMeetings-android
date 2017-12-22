package edu.calbaptist.android.projectmeetings.utils.rest;

import org.json.JSONObject;

/**
 *  Rest Client JSON Callback Interface
 *  Specifies the functionality needed to handle a JSON callback
 *
 *  @author Caleb Solorio
 *  @version 1.0.0 12/20/17
 */

public interface RestClientJsonCallback extends RestClientCallbackFailure {
    /**
     * Handles the JSONObject passed back from RestClient
     * @param json the JSONObject passed back.
     */
    void onTaskExecuted(JSONObject json);
}
