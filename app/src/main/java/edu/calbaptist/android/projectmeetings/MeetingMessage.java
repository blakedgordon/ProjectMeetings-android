package edu.calbaptist.android.projectmeetings;

import java.io.Serializable;

/**
 *  Meeting Message
 *  Stores information about a message sent during a meeting.
 *
 *  @author Caleb Solorio
 *  @version 0.7.0 12/3/17
 */
public class MeetingMessage implements Serializable {
    private String msg;
    private boolean isFile;

    /**
     * Creates a new MeetingMessage object.
     * @param msg the message text.
     * @param isFile whether or not the message is a file upload.
     */
    public MeetingMessage(String msg, boolean isFile) {
        this.msg = msg;
        this.isFile = isFile;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }
}
