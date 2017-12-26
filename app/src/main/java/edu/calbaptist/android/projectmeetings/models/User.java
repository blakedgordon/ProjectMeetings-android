package edu.calbaptist.android.projectmeetings.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  User
 *  Stores and provides user data.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/27/17
 */
public class User implements Serializable {
    public static final String TAG = "User";

    private String mUID;
    private String mEmail;
    private String mDisplayName;
    private String mFirebaseToken;
    private String mGoogleToken;
    private String mInstanceId;
    private ArrayList mInvites;
    private ArrayList mMeetings;

    private User(UserBuilder builder) {
        this.mUID = builder.mUId;
        this.mEmail = builder.mEmail;
        this.mDisplayName = builder.mDisplayName;
        this.mFirebaseToken = builder.mFirebaseToken;
        this.mGoogleToken = builder.mGoogleToken;
        this.mInstanceId = builder.mInstanceId;
        this.mInvites = builder.mInvites;
        this.mMeetings = builder.mMeetings;
    }

    /**
     * Gets the user's mUId.
     * @return the mUID string
     */
    public String getUId() {
        return mUID;
    }

    /**
     * Gets the user's mEmail.
     * @return the mEmail string.
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Gets the user's display name.
     * @return the display name string.
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     * Gets the user's Firebase authentication token.
     * @return the Firebase authentication token string.
     */
    public String getFirebaseToken() {
        return mFirebaseToken;
    }

    /**
     * Gets the user's Google authentication token.
     * @return the Google authentication token. string.
     */
    public String getGoogleToken() {
        return mGoogleToken;
    }

    /**
     * Gets the user's instance id.
     * @return the instance id string.
     */
    public String getInstanceId() {
        return mInstanceId;
    }

    /**
     * Gets the user's mInvites.
     * @return the mInvites list.
     */
    public ArrayList getInvites() {
        return mInvites;
    }

    /**
     * Gets the user's mMeetings.
     * @return the mMeetings list.
     */
    public ArrayList getMeetings() {
        return mMeetings;
    }

    /**
     *  User Builder
     *  Creates a User object.
     *
     *  @author Caleb Solorio
     *  @version 0.4.0 11/27/17
     */
    public static class UserBuilder {
        private String mUId;
        private String mEmail;
        private String mDisplayName;

        private String mFirebaseToken;
        private String mGoogleToken;
        private String mInstanceId;
        private ArrayList mInvites;
        private ArrayList mMeetings;

        /**
         * Sets the builder's uid.
         * @param uid the mUID of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setUid(String uid) {
            this.mUId = uid;
            return this;
        }

        /**
         * Sets the builder's email.
         * @param email the mEmail of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setEmail(String email) {
            this.mEmail = email;
            return this;
        }

        /**
         * Sets the builder's display name.
         * @param displayName the display name of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setDisplayName(String displayName) {
            this.mDisplayName = displayName;
            return this;
        }

        /**
         * Sets the builder's Firebase token.
         * @param firebaseToken the Firebase token of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setFirebaseToken(String firebaseToken) {
            this.mFirebaseToken = firebaseToken;
            return this;
        }

        /**
         * Sets the builder's Google token.
         * @param googleToken the Google token of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setGoogleToken(String googleToken) {
            this.mGoogleToken = googleToken;
            return this;
        }

        /**
         * Sets the builder's instance id.
         * @param instanceId the instance id of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setInstanceId(String instanceId) {
            this.mInstanceId = instanceId;
            return this;
        }

        /**
         * Sets the builder's invites.
         * @param invites the mInvites of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setInvites(ArrayList invites) {
            this.mInvites = invites;
            return this;
        }

        /**
         * Sets the builder's meetings.
         * @param meetings the mMeetings of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setMeetings(ArrayList meetings) {
            this.mMeetings = meetings;
            return this;
        }

        /**
         * Builds a User based off of the data provided.
         * @return a new User object.
         */
        public User build() {
            return new User(this);
        }
    }

}
