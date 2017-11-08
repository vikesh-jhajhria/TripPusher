package com.trippusher.vo;

import android.support.annotation.NonNull;

/**
 * Created by Vikesh on 09-Nov-17.
 */

public class MessageVo implements Comparable<MessageVo>{
    private String message_from;
    private String message_to;
    private String message_time;
    private String message_text;

    public String getMessage_from() {
        return message_from;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_to() {
        return message_to;
    }

    public void setMessage_to(String message_to) {
        this.message_to = message_to;
    }

    public String getMessage_time() {
        return message_time;
    }

    public void setMessage_time(String message_time) {
        this.message_time = message_time;
    }

    public String getMessage_text() {
        return message_text;
    }

    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }

    public String getIs_read() {
        return is_read;
    }

    public void setIs_read(String is_read) {
        this.is_read = is_read;
    }

    private String is_read;

    @Override
    public int compareTo(@NonNull MessageVo messageVo) {
        return this.getMessage_time().compareTo(messageVo.getMessage_time());
    }
}
