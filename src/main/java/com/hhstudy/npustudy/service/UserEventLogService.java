package com.HHStudy.npustudy.service;

import com.HHStudy.npustudy.domain.UserEventLog;
import com.HHStudy.npustudy.viewmodel.admin.user.UserEventPageRequestVM;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserEventLogService extends BaseService<UserEventLog> {

    List<UserEventLog> getUserEventLogByUserId(Integer id);

    PageInfo<UserEventLog> page(UserEventPageRequestVM requestVM);

    List<Integer> selectMothCount();
}
