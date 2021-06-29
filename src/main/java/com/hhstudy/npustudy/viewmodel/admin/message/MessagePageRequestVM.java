package com.hhstudy.npustudy.viewmodel.admin.message;

import com.HHStudy.npustudy.base.BasePage;


public class MessagePageRequestVM extends BasePage {
    private String sendUserName;

    public String getSendUserName() {
        return sendUserName;
    }

    public void setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
    }
}
