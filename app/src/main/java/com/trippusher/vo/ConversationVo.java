package com.trippusher.vo;

import java.util.ArrayList;

/**
 * Created by Vikesh on 09-Nov-17.
 */

public class ConversationVo {
    private String name;
    private String email;
    private ArrayList<MessageVo> messageVo;

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

    public ArrayList<MessageVo> getMessageList() {
        return messageVo;
    }

    public void setMessageList(ArrayList<MessageVo> messageVo) {
        this.messageVo = messageVo;
    }


}
