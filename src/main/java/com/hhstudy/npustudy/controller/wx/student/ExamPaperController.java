package com.hhstudy.npustudy.controller.wx.student;

import com.HHStudy.npustudy.base.RestResponse;
import com.HHStudy.npustudy.controller.wx.BaseWXApiController;
import com.HHStudy.npustudy.domain.ExamPaper;
import com.HHStudy.npustudy.domain.Subject;
import com.HHStudy.npustudy.service.ExamPaperService;
import com.HHStudy.npustudy.service.SubjectService;
import com.HHStudy.npustudy.utility.DateTimeUtil;
import com.HHStudy.npustudy.utility.PageInfoHelper;
import com.HHStudy.npustudy.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperPageResponseVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperPageVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;


@Controller("WXStudentExamController")
@RequestMapping(value = "/api/wx/student/exampaper")
@ResponseBody
public class ExamPaperController extends BaseWXApiController {

    private final ExamPaperService examPaperService;
    private final SubjectService subjectService;

    @Autowired
    public ExamPaperController(ExamPaperService examPaperService, SubjectService subjectService) {
        this.examPaperService = examPaperService;
        this.subjectService = subjectService;
    }


    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<ExamPaperEditRequestVM> select(@PathVariable Integer id) {
        ExamPaperEditRequestVM vm = examPaperService.examPaperToVM(id);
        return RestResponse.ok(vm);
    }


    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamPaperPageResponseVM>> pageList(@Valid ExamPaperPageVM model) {
        model.setLevelId(getCurrentUser().getUserLevel());
        PageInfo<ExamPaper> pageInfo = examPaperService.studentPage(model);
        PageInfo<ExamPaperPageResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamPaperPageResponseVM vm = modelMapper.map(e, ExamPaperPageResponseVM.class);
            Subject subject = subjectService.selectById(vm.getSubjectId());
            vm.setSubjectName(subject.getName());
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }
}
