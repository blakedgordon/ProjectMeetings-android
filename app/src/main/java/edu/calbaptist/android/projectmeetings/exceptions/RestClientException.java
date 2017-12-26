package edu.calbaptist.android.projectmeetings.exceptions;

import org.json.JSONObject;

/**
 *  Rest Client Exception
 *  Thrown in case of a bad response from the RestClient class.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/27/17
 */
public class RestClientException extends Exception {
    private int mResponseCode;
    private JSONObject mJson;

    /**
     * Initializes a new RestClientException.
     * @param responseCode the corresponding HTTP code from the API's response.
     * @param json the JSON given back by the API.
     */
    public RestClientException(int responseCode, JSONObject json) {
        this.mResponseCode = responseCode;
        this.mJson = json;
    }

    /**
     * Get the error code from the API's response.
     * @return the mResponseCode attribute.
     */
    public int getResponseCode() {
        return mResponseCode;
    }

    /**
     * Get the JSON from the API's response.
     * @return the mJson attribute.
     */
    public JSONObject getJson() {
        return mJson;
    }
}
