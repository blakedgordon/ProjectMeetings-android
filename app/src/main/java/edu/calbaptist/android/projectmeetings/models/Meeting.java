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
    public static final String TAG = "Meeting";

    private String mId;
    private String mUId;
    private String mName;
    private String mObjective;
    private long mTime;
    private long mTimeLimit;
    private String mDriveFolderId;
    private ArrayList mInvites;

    private Meeting(MeetingBuilder builder) {
        this.mId = builder.mId;
        this.mUId = builder.mUId;
        this.mName = builder.mName;
        this.mObjective = builder.mObjective;
        this.mTime = builder.mTime;
        this.mTimeLimit = builder.mTimeLimit;
        this.mDriveFolderId = builder.mDriveFolderId;
        this.mInvites = builder.mInvites;
    }

    /**
     * Gets the meetings's mId.
     * @return the mId string.
     */
    public String getMId() {
        return mId;
    }

    /**
     * Gets the meetings's uId.
     * @return the mUId string.
     */
    public String getUId() {
        return mUId;
    }

    /**
     * Gets the meetings's name.
     * @return the mName string.
     */
    public String getName() {
        return mName;
    }

    /**
     * Gets the meetings's objective.
     * @return the mObjective string.
     */
    public String getObjective() {
        return mObjective;
    }

    /**
     * Gets the meetings's time.
     * @return the mTime long.
     */
    public long getTime() {
        return mTime;
    }

    /**
     * Gets the meetings's time limit.
     * @return the mTimeLimit long.
     */
    public long getTimeLimit() {
        return mTimeLimit;
    }

    /**
     * Gets the meetings's drive folder id.
     * @return the mDriveFolderId string.
     */
    public String getDriveFolderId() {
        return mDriveFolderId;
    }

    /**
     * Gets the meetings's invites.
     * @return the mInvites list
     */
    public ArrayList getInvites() {
        return mInvites;
    }

    /**
     *  Meeting Builder
     *  Creates a Meeting object.
     *
     *  @author Caleb Solorio
     *  @version 0.4.0 11/27/17
     */
    public static class MeetingBuilder {
        private String mId;
        private String mUId;
        private String mName;
        private String mObjective;
        private long mTime;
        private long mTimeLimit;
        private String mDriveFolderId;
        private ArrayList mInvites;

        /**
         * Sets the builder's mId.
         * @param mid the mId of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setMId(String mid) {
            this.mId = mid;
            return this;
        }

        /**
         * Sets the builder's mUId.
         * @param uid the mUId of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setUId(String uid) {
            this.mUId = uid;
            return this;
        }

        /**
         * Sets the builder's mName.
         * @param name the mName of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setName(String name) {
            this.mName = name;
            return this;
        }

        /**
         * Sets the builder's mObjective.
         * @param objective the mObjective of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setObjective(String objective) {
            this.mObjective = objective;
            return this;
        }

        /**
         * Sets the builder's mTime.
         * @param time the mTime of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setTime(long time) {
            this.mTime = time;
            return this;
        }

        /**
         * Sets the builder's mTime limit.
         * @param timeLimit the mTime limit of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setTimeLimit(long timeLimit) {
            this.mTimeLimit = timeLimit;
            return this;
        }

        /**
         * Sets the builder's drive folder id.
         * @param driveFolderId the drive folder id of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setDriveFolderId(String driveFolderId) {
            this.mDriveFolderId = driveFolderId;
            return this;
        }

        /**
         * Sets the builder's mInvites.
         * @param invites the mInvites of the user.
         * @return the new MeetingBuilder object.
         */
        public MeetingBuilder setInvites(ArrayList invites) {
            this.mInvites = invites;
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
