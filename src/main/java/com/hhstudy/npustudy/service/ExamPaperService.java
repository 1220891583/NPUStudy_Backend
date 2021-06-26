package com.HHStudy.npustudy.service;

import com.HHStudy.npustudy.domain.ExamPaper;
import com.HHStudy.npustudy.domain.User;
import com.HHStudy.npustudy.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.HHStudy.npustudy.viewmodel.admin.exam.ExamPaperPageRequestVM;
import com.HHStudy.npustudy.viewmodel.student.dashboard.PaperFilter;
import com.HHStudy.npustudy.viewmodel.student.dashboard.PaperInfo;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperPageVM;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ExamPaperService extends BaseService<ExamPaper> {

    PageInfo<ExamPaper> page(ExamPaperPageRequestVM requestVM);

    PageInfo<ExamPaper> taskExamPage(ExamPaperPageRequestVM requestVM);

    PageInfo<ExamPaper> studentPage(ExamPaperPageVM requestVM);

    ExamPaper savePaperFromVM(ExamPaperEditRequestVM examPaperEditRequestVM, User user);

    ExamPaperEditRequestVM examPaperToVM(Integer id);

    List<PaperInfo> indexPaper(PaperFilter paperFilter);

    Integer selectAllCount();

    List<Integer> selectMothCount();
}
