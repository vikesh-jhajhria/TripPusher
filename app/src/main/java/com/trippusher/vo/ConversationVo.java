package com.trippusher.vo;

import java.util.ArrayList;

/**
 * Created by Vikesh on 09-Nov-17.
 */

public class ConversationVo {


    private String key = "";
    private String name = "";
    private String email = "";
    private MessageVo messageVo;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MessageVo getMessageVo() {
        return messageVo;
    }

    public void setMessageVo(MessageVo messageVo) {
        this.messageVo = messageVo;
    }


}
