package edu.calbaptist.android.projectmeetings.java_phoenix_channels;

public interface IMessageCallback {

    /**
     * @param envelope The envelope containing the message payload and properties
     */
    void onMessage(final Envelope envelope);
}
