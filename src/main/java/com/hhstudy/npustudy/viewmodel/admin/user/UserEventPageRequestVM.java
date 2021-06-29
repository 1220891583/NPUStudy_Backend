package com.hhstudy.npustudy.viewmodel.admin.user;

import com.HHStudy.npustudy.base.BasePage;


/**
 * @author HHStudyGroup
 */


public class UserEventPageRequestVM extends BasePage {

    private Integer userId;

    private String userName;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
