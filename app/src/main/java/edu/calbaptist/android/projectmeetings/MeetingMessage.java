package edu.calbaptist.android.projectmeetings;

import java.io.Serializable;

/**
 * Created by csolo on 11/30/2017.
 */

public class MeetingMessage implements Serializable {
    private String msg;
    private boolean isFile;

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
