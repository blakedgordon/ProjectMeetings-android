package edu.calbaptist.android.projectmeetings;

import com.google.firebase.database.DatabaseReference;

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
    private static final String TAG = "User";

    private DatabaseReference db;

    private String uid;
    private String email;
    private String displayName;
    private String firebaseToken;
    private String googleToken;
    private String instanceId;
    private ArrayList invites;
    private ArrayList meetings;

    private User(UserBuilder builder) {
        this.uid = builder.uid;
        this.email = builder.email;
        this.displayName = builder.displayName;
        this.firebaseToken = builder.firebaseToken;
        this.googleToken = builder.googleToken;
        this.instanceId = builder.instanceId;
        this.invites = builder.invites;
        this.meetings = builder.meetings;
    }

    /**
     * Gets the user's uid.
     * @return the uid string
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the user's email.
     * @return the email string.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's display name.
     * @return the display name string.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the user's Firebase authentication token.
     * @return the Firebase authentication token string.
     */
    public String getFirebaseToken() {
        return firebaseToken;
    }

    /**
     * Gets the user's Google authentication token.
     * @return the Google authentication token. string.
     */
    public String getGoogleToken() {
        return googleToken;
    }

    /**
     * Gets the user's instance id.
     * @return the instance id string.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Gets the user's add_invites.
     * @return the add_invites list.
     */
    public ArrayList getInvites() {
        return invites;
    }

    /**
     * Gets the user's meetings.
     * @return the meetings list.
     */
    public ArrayList getMeetings() {
        return meetings;
    }

    /**
     *  User Builder
     *  Creates a User object.
     *
     *  @author Caleb Solorio
     *  @version 0.4.0 11/27/17
     */
    public static class UserBuilder {
        private String uid;
        private String email;
        private String displayName;

        private String firebaseToken;
        private String googleToken;
        private String instanceId;
        private ArrayList invites;
        private ArrayList meetings;

        /**
         * Sets the builder's uid.
         * @param uid the uid of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        /**
         * Sets the builder's email.
         * @param email the email of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        /**
         * Sets the builder's display name.
         * @param displayName the display name of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the builder's Firebase token.
         * @param firebaseToken the Firebase token of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setFirebaseToken(String firebaseToken) {
            this.firebaseToken = firebaseToken;
            return this;
        }

        /**
         * Sets the builder's Google token.
         * @param googleToken the Google token of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setGoogleToken(String googleToken) {
            this.googleToken = googleToken;
            return this;
        }

        /**
         * Sets the builder's instance id.
         * @param instanceId the instance id of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setInstanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        /**
         * Sets the builder's add_invites.
         * @param invites the add_invites of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setInvites(ArrayList invites) {
            this.invites = invites;
            return this;
        }

        /**
         * Sets the builder's meetings.
         * @param meetings the meetings of the user.
         * @return the new UserBuilder object.
         */
        public UserBuilder setMeetings(ArrayList meetings) {
            this.meetings = meetings;
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
