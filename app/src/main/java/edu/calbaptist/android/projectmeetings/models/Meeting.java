package edu.calbaptist.android.projectmeetings.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *  Meeting
 *  Stores and provides meeting data.
 *
 *  @author Caleb Solorio
 *  @version 0.4.0 11/27/17
 */
public class Meeting implements Serializable{
    private static final String TAG = "Meeting";

    private String mid;
    private String uid;
    private String name;
    private String objective;
    private long time;
    private long timeLimit;
    private String driveFolderId;
    private ArrayList invites;

    private Meeting(MeetingBuilder builder) {
        this.mid = builder.mid;
        this.uid = builder.uid;
        this.name = builder.name;
        this.objective = builder.objective;
        this.time = builder.time;
        this.timeLimit = builder.timeLimit;
        this.driveFolderId = builder.driveFolderId;
        this.invites = builder.invites;
    }

    /**
     * Gets the meetings's mid.
     * @return the mid string.
     */
    public String getMid() {
        return mid;
    }

    /**
     * Gets the meetings's uid.
     * @return the uid string.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the meetings's name.
     * @return the name string.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the meetings's objective.
     * @return the objective string.
     */
    public String getObjective() {
        return objective;
    }

    /**
     * Gets the meetings's time.
     * @return the time.
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the meetings's time limit.
     * @return the time limit.
     */
    public long getTimeLimit() {
        return timeLimit;
    }

    /**
     * Gets the meetings's drive folder id.
     * @return the drive folder id string.
     */
    public String getDriveFolderId() {
        return driveFolderId;
    }

    /**
     * Gets the meetings's add_invites.
     * @return the add_invites list
     */
    public ArrayList getInvites() {
        return invites;
    }

    /**
     *  Meeting Builder
     *  Creates a Meeting object.
     *
     *  @author Caleb Solorio
     *  @version 0.4.0 11/27/17
     */
    public static class MeetingBuilder {
        private String mid;
        private String uid;
        private String name;
        private String objective;
        private long time;
        private long timeLimit;
        private String driveFolderId;
        private ArrayList invites;

        /**
         * Sets the builder's mid.
         * @param mid the mid of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        /**
         * Sets the builder's uid.
         * @param uid the uid of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        /**
         * Sets the builder's name.
         * @param name the name of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the builder's objective.
         * @param objective the objective of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setObjective(String objective) {
            this.objective = objective;
            return this;
        }

        /**
         * Sets the builder's time.
         * @param time the time of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setTime(long time) {
            this.time = time;
            return this;
        }

        /**
         * Sets the builder's time limit.
         * @param timeLimit the time limit of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setTimeLimit(long timeLimit) {
            this.timeLimit = timeLimit;
            return this;
        }

        /**
         * Sets the builder's drive folder id.
         * @param driveFolderId the drive folder id of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setDriveFolderId(String driveFolderId) {
            this.driveFolderId = driveFolderId;
            return this;
        }

        /**
         * Sets the builder's add_invites.
         * @param invites the add_invites of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setInvites(ArrayList invites) {
            this.invites = invites;
            return this;
        }

        /**
         * Builds a Meeting based off of the data provided.
         * @return a new Meeting object.
         */
        public Meeting build() {
            return new Meeting(this);
        }
    }
}
