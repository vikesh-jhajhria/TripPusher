package com.trippusher.vo;

import android.support.annotation.NonNull;

/**
 * Created by Vikesh on 09-Nov-17.
 */

public class MessageVo implements Comparable<MessageVo>{
    private String content;
    private String fromID;
    private Boolean isRead;
    private int timestamp;
    private String toID;
    private String type;

    public MessageVo() {
        // empty constructor
    }

    public String getcontent() {
        return content;
    }

    public void setcontent(String contents) {
        content = contents;
    }

    public String getfromID() {
        return fromID;
    }

    public void setfromID(String fromid) {
        fromID = fromid;
    }

    public Boolean getisRead() {
        return isRead;
    }

    public void setisRead(Boolean isread) {
        isRead = isread;
    }

    public int gettimestamp() {
        return timestamp;
    }

    public void settimestamp(int timestamps) {
        timestamp = timestamps;
    }

    public String gettoID() {
        return toID;
    }

    public void settoID(String toid) {
        toID = toid;
    }

    public String gettype() {
        return type;
    }

    public void settype(String types) {
        type = types;
    }

    @Override
    public int compareTo(@NonNull MessageVo messageVo) {
        return String.valueOf(this.gettimestamp()).compareTo(String.valueOf(messageVo.gettimestamp()));
    }
}
