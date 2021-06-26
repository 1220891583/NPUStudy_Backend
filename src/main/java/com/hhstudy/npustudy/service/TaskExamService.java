package com.HHStudy.npustudy.service;

import com.HHStudy.npustudy.domain.TaskExam;
import com.HHStudy.npustudy.domain.User;
import com.HHStudy.npustudy.viewmodel.admin.task.TaskPageRequestVM;
import com.HHStudy.npustudy.viewmodel.admin.task.TaskRequestVM;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface TaskExamService extends BaseService<TaskExam> {

    PageInfo<TaskExam> page(TaskPageRequestVM requestVM);

    void edit(TaskRequestVM model, User user);

    TaskRequestVM taskExamToVM(Integer id);

    List<TaskExam> getByGradeLevel(Integer gradeLevel);
}
