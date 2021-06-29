package com.hhstudy.npustudy.controller.wx.student;

import com.HHStudy.npustudy.base.RestResponse;
import com.HHStudy.npustudy.controller.wx.BaseWXApiController;
import com.HHStudy.npustudy.domain.*;
import com.HHStudy.npustudy.domain.enums.QuestionTypeEnum;
import com.HHStudy.npustudy.event.CalculateExamPaperAnswerCompleteEvent;
import com.HHStudy.npustudy.event.UserEvent;
import com.HHStudy.npustudy.service.ExamPaperAnswerService;
import com.HHStudy.npustudy.service.ExamPaperService;
import com.HHStudy.npustudy.service.SubjectService;
import com.HHStudy.npustudy.utility.DateTimeUtil;
import com.HHStudy.npustudy.utility.ExamUtil;
import com.HHStudy.npustudy.utility.PageInfoHelper;
import com.HHStudy.npustudy.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperReadVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperSubmitItemVM;
import com.HHStudy.npustudy.viewmodel.student.exam.ExamPaperSubmitVM;
import com.HHStudy.npustudy.viewmodel.student.exampaper.ExamPaperAnswerPageResponseVM;
import com.HHStudy.npustudy.viewmodel.student.exampaper.ExamPaperAnswerPageVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;


@Controller("WXStudentExamPaperAnswerController")
@RequestMapping(value = "/api/wx/student/exampaper/answer")
@ResponseBody
public class ExamPaperAnswerController extends BaseWXApiController {

    private final ExamPaperAnswerService examPaperAnswerService;
    private final SubjectService subjectService;
    private final ApplicationEventPublisher eventPublisher;
    private final ExamPaperService examPaperService;

    @Autowired
    public ExamPaperAnswerController(ExamPaperAnswerService examPaperAnswerService, SubjectService subjectService, ApplicationEventPublisher eventPublisher, ExamPaperService examPaperService) {
        this.examPaperAnswerService = examPaperAnswerService;
        this.subjectService = subjectService;
        this.eventPublisher = eventPublisher;
        this.examPaperService = examPaperService;
    }

    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamPaperAnswerPageResponseVM>> pageList(@Valid ExamPaperAnswerPageVM model) {
        model.setCreateUser(getCurrentUser().getId());
        PageInfo<com.HHStudy.npustudy.domain.ExamPaperAnswer> pageInfo = examPaperAnswerService.studentPage(model);
        PageInfo<ExamPaperAnswerPageResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamPaperAnswerPageResponseVM vm = modelMapper.map(e, ExamPaperAnswerPageResponseVM.class);
            com.HHStudy.npustudy.domain.Subject subject = subjectService.selectById(vm.getSubjectId());
            vm.setDoTime(ExamUtil.secondToVM(e.getDoTime()));
            vm.setSystemScore(ExamUtil.scoreToVM(e.getSystemScore()));
            vm.setUserScore(ExamUtil.scoreToVM(e.getUserScore()));
            vm.setPaperScore(ExamUtil.scoreToVM(e.getPaperScore()));
            vm.setSubjectName(subject.getName());
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }


    @RequestMapping(value = "/answerSubmit", method = RequestMethod.POST)
    public RestResponse answerSubmit(HttpServletRequest request) {
        ExamPaperSubmitVM examPaperSubmitVM = requestToExamPaperSubmitVM(request);
        com.HHStudy.npustudy.domain.User user = getCurrentUser();
        com.HHStudy.npustudy.domain.ExamPaperAnswerInfo examPaperAnswerInfo = examPaperAnswerService.calculateExamPaperAnswer(examPaperSubmitVM, user);
        if (null == examPaperAnswerInfo) {
            return RestResponse.fail(2, "试卷不能重复做");
        }
        com.HHStudy.npustudy.domain.ExamPaperAnswer examPaperAnswer = examPaperAnswerInfo.getExamPaperAnswer();
        Integer userScore = examPaperAnswer.getUserScore();
        String scoreVm = ExamUtil.scoreToVM(userScore);
        com.HHStudy.npustudy.domain.UserEventLog userEventLog = new com.HHStudy.npustudy.domain.UserEventLog(user.getId(), user.getUserName(), user.getRealName(), new Date());
        String content = user.getUserName() + " 提交试卷：" + examPaperAnswerInfo.getExamPaper().getName()
                + " 得分：" + scoreVm
                + " 耗时：" + ExamUtil.secondToVM(examPaperAnswer.getDoTime());
        userEventLog.setContent(content);
        eventPublisher.publishEvent(new CalculateExamPaperAnswerCompleteEvent(examPaperAnswerInfo));
        eventPublisher.publishEvent(new UserEvent(userEventLog));
        return RestResponse.ok(scoreVm);
    }

    private ExamPaperSubmitVM requestToExamPaperSubmitVM(HttpServletRequest request) {
        ExamPaperSubmitVM examPaperSubmitVM = new ExamPaperSubmitVM();
        examPaperSubmitVM.setId(Integer.parseInt(request.getParameter("id")));
        examPaperSubmitVM.setDoTime(Integer.parseInt(request.getParameter("doTime")));
        List<String> parameterNames = Collections.list(request.getParameterNames()).stream()
                .filter(n -> n.contains("_"))
                .collect(Collectors.toList());
        //题目答案按序号分组
        Map<String, List<String>> questionGroup = parameterNames.stream().collect(Collectors.groupingBy(p -> p.substring(0, p.indexOf("_"))));
        List<ExamPaperSubmitItemVM> answerItems = new ArrayList<>();
        questionGroup.forEach((k, v) -> {
            ExamPaperSubmitItemVM examPaperSubmitItemVM = new ExamPaperSubmitItemVM();
            String p = v.get(0);
            String[] keys = p.split("_");
            examPaperSubmitItemVM.setQuestionId(Integer.parseInt(keys[1]));
            examPaperSubmitItemVM.setItemOrder(Integer.parseInt(keys[0]));
            QuestionTypeEnum typeEnum = QuestionTypeEnum.fromCode(Integer.parseInt(keys[2]));
            if (v.size() == 1) {
                String content = request.getParameter(p);
                examPaperSubmitItemVM.setContent(content);
                if (typeEnum == QuestionTypeEnum.MultipleChoice) {
                    examPaperSubmitItemVM.setContentArray(Arrays.asList(content.split(",")));
                }
            } else {  //多个空 填空题
                List<String> answers = v.stream().sorted(Comparator.comparingInt(ExamUtil::lastNum)).map(inputKey -> request.getParameter(inputKey)).collect(Collectors.toList());
                examPaperSubmitItemVM.setContentArray(answers);
            }
            answerItems.add(examPaperSubmitItemVM);
        });
        examPaperSubmitVM.setAnswerItems(answerItems);
        return examPaperSubmitVM;
    }


    @PostMapping(value = "/read/{id}")
    public RestResponse<ExamPaperReadVM> read(@PathVariable Integer id) {
        ExamPaperReadVM vm = new ExamPaperReadVM();
        com.HHStudy.npustudy.domain.ExamPaperAnswer examPaperAnswer = examPaperAnswerService.selectById(id);
        ExamPaperEditRequestVM paper = examPaperService.examPaperToVM(examPaperAnswer.getExamPaperId());
        ExamPaperSubmitVM answer = examPaperAnswerService.examPaperAnswerToVM(examPaperAnswer.getId());
        vm.setPaper(paper);
        vm.setAnswer(answer);
        return RestResponse.ok(vm);
    }
}
