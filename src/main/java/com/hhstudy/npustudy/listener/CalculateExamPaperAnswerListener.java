package com.hhstudy.npustudy.listener;

import com.HHStudy.npustudy.domain.*;
import com.HHStudy.npustudy.domain.enums.ExamPaperTypeEnum;
import com.HHStudy.npustudy.domain.enums.QuestionTypeEnum;
import com.HHStudy.npustudy.event.CalculateExamPaperAnswerCompleteEvent;
import com.HHStudy.npustudy.service.ExamPaperAnswerService;
import com.HHStudy.npustudy.service.ExamPaperQuestionCustomerAnswerService;
import com.HHStudy.npustudy.service.TaskExamCustomerAnswerService;
import com.HHStudy.npustudy.service.TextContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * @author HHStudyGroup
 */
@Component
public class CalculateExamPaperAnswerListener implements ApplicationListener<CalculateExamPaperAnswerCompleteEvent> {

    private final ExamPaperAnswerService examPaperAnswerService;
    private final ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService;
    private final TextContentService textContentService;
    private final TaskExamCustomerAnswerService examCustomerAnswerService;

    @Autowired
    public CalculateExamPaperAnswerListener(ExamPaperAnswerService examPaperAnswerService, ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService, TextContentService textContentService, TaskExamCustomerAnswerService examCustomerAnswerService) {
        this.examPaperAnswerService = examPaperAnswerService;
        this.examPaperQuestionCustomerAnswerService = examPaperQuestionCustomerAnswerService;
        this.textContentService = textContentService;
        this.examCustomerAnswerService = examCustomerAnswerService;
    }

    @Override
    @Transactional
    public void onApplicationEvent(CalculateExamPaperAnswerCompleteEvent calculateExamPaperAnswerCompleteEvent) {
        Date now = new Date();

        com.HHStudy.npustudy.domain.ExamPaperAnswerInfo examPaperAnswerInfo = (com.HHStudy.npustudy.domain.ExamPaperAnswerInfo) calculateExamPaperAnswerCompleteEvent.getSource();
        com.HHStudy.npustudy.domain.ExamPaper examPaper = examPaperAnswerInfo.getExamPaper();
        com.HHStudy.npustudy.domain.ExamPaperAnswer examPaperAnswer = examPaperAnswerInfo.getExamPaperAnswer();
        List<com.HHStudy.npustudy.domain.ExamPaperQuestionCustomerAnswer> examPaperQuestionCustomerAnswers = examPaperAnswerInfo.getExamPaperQuestionCustomerAnswers();

        examPaperAnswerService.insertByFilter(examPaperAnswer);
        examPaperQuestionCustomerAnswers.stream().filter(a -> QuestionTypeEnum.needSaveTextContent(a.getQuestionType())).forEach(d -> {
            com.HHStudy.npustudy.domain.TextContent textContent = new com.HHStudy.npustudy.domain.TextContent(d.getAnswer(), now);
            textContentService.insertByFilter(textContent);
            d.setTextContentId(textContent.getId());
            d.setAnswer(null);
//            d.setAnswer(d.getAnswer());
        });
        examPaperQuestionCustomerAnswers.forEach(d -> {
            d.setExamPaperAnswerId(examPaperAnswer.getId());
        });
        examPaperQuestionCustomerAnswerService.insertList(examPaperQuestionCustomerAnswers);

        switch (ExamPaperTypeEnum.fromCode(examPaper.getPaperType())) {
            case Task: {
                examCustomerAnswerService.insertOrUpdate(examPaper, examPaperAnswer, now);
                break;
            }
            default:
                break;
        }
    }
}
